/*
 *    Copyright 2019 Andrey Mukamolov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.ui.map

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.util.EXTRA_COORDINATES
import com.bookcrossing.mobile.util.MapDelegate
import com.bookcrossing.mobile.util.observe
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

/**
 * A fragment that shows map for book's location as a modal bottom sheet.
 */
class BookLocationBottomSheet : BottomSheetDialogFragment(), OnMapReadyCallback {

  @BindView(R.id.book_location_map)
  lateinit var mapView: MapView

  @BindView(R.id.book_location_header)
  lateinit var toolbar: MaterialToolbar

  private lateinit var mapDelegate: MapDelegate
  private lateinit var unbinder: Unbinder
  private lateinit var permissions: RxPermissions
  private lateinit var locationProvider: FusedLocationProviderClient

  private val subscriptions = CompositeDisposable()

  // workaround for map gestures : disable bottom sheet dragging to be able to use map gestures
  private val bottomSheetCallback: BottomSheetCallback by lazy(mode = NONE) {
    object : BottomSheetCallback() {
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        // do nothing
      }

      override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == STATE_DRAGGING) {
          BottomSheetBehavior.from(bottomSheet).state = STATE_COLLAPSED
        }
      }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_book_location_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    unbinder = ButterKnife.bind(this, view)
    permissions = RxPermissions(this)
    locationProvider =
      LocationServices.getFusedLocationProviderClient(requireActivity())

    mapDelegate = MapDelegate(mapView, viewLifecycleOwner)

    mapView.getMapAsync(this)

    subscriptions.add(
      toolbar.navigationClicks()
        .subscribe {
          dismiss()
        }
    )
  }

  override fun onMapReady(googleMap: GoogleMap) {
    Timber.d("Map loaded")

    val bookCoordinates: Coordinates? = requireArguments().getParcelable(EXTRA_COORDINATES)
    if (bookCoordinates != null) {
      val bookLocation = LatLng(
        bookCoordinates.lat ?: 0.0,
        bookCoordinates.lng ?: 0.0
      )

      googleMap.addMarker(MarkerOptions().position(bookLocation))

      googleMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
          bookLocation,
          DEFAULT_ZOOM_LEVEL
        )
      )
    } else {
      setupCurrentLocation()
    }
  }

  @SuppressLint("MissingPermission") // permission is checked in RxPermission
  private fun setupCurrentLocation() {
    subscriptions.add(
      permissions.request(ACCESS_FINE_LOCATION)
        .flatMapSingle {
          locationProvider.lastLocation.observe()
        }
        .map { LatLng(it.latitude, it.longitude) }
        .subscribe({
          mapDelegate.setupCurrentLocation(it)
        }, { error ->
          Timber.e(error)
          Snackbar.make(mapView, string.location_permission_denied_prompt, Snackbar.LENGTH_LONG)
            .show()
          dismiss()
        })
    )
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.behavior.addBottomSheetCallback(bottomSheetCallback)
    return dialog
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapDelegate.onLowMemory()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    unbinder.unbind()
    subscriptions.clear()
  }

  override fun onCancel(dialog: DialogInterface) {
    (dialog as BottomSheetDialog).behavior.removeBottomSheetCallback(bottomSheetCallback)
    subscriptions.clear()
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    onCancel(dialog)
  }

  companion object {
    const val TAG = "BookLocationBottomSheet"
    const val DEFAULT_ZOOM_LEVEL = 16.0f

    /** Create new bottom sheet to display given coordinates */
    fun newInstance(bookCoordinates: Coordinates?): BookLocationBottomSheet {
      return BookLocationBottomSheet().apply {
        arguments = bundleOf(EXTRA_COORDINATES to bookCoordinates)
      }
    }
  }
}