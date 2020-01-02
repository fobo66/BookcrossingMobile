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
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.util.MapDelegate
import com.bookcrossing.mobile.util.observe
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE


/**
 * A fragment that shows map for picking book's location as a modal bottom sheet.
 */
class LocationPicker : BottomSheetDialogFragment(), OnMapReadyCallback {

  @BindView(R.id.book_location_picker_map)
  lateinit var mapView: MapView

  @BindView(R.id.book_location_picker_header)
  lateinit var toolbar: MaterialToolbar

  @BindView(R.id.book_location_picker_button)
  lateinit var pickLocationButton: Button

  private lateinit var mapDelegate: MapDelegate
  private lateinit var unbinder: Unbinder
  private lateinit var permissions: RxPermissions
  private lateinit var locationProvider: FusedLocationProviderClient

  private var bookLocation: Marker? = null

  private val subscriptions = CompositeDisposable()

  private val bookLocationPicked = PublishSubject.create<Coordinates>()

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

  /** Observe book location picked*/
  fun onBookLocationPicked(): Observable<Coordinates> = bookLocationPicked.hide()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_location_picker, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    unbinder = ButterKnife.bind(this, view)
    permissions = RxPermissions(this)
    locationProvider =
      LocationServices.getFusedLocationProviderClient(requireActivity())

    mapDelegate = MapDelegate(mapView, viewLifecycleOwner)

    setupCurrentLocation()

    mapView.getMapAsync(this)

    subscriptions.add(toolbar.navigationClicks()
      .subscribe { dismiss() })

    subscriptions.add(
      pickLocationButton.clicks()
        .subscribe {
          bookLocationPicked.onNext(Coordinates(bookLocation?.position))
          dismiss()
      })
  }

  override fun onMapReady(googleMap: GoogleMap) {
    Timber.d("Map loaded")

    googleMap.setOnMapClickListener {
      if (bookLocation == null) {
        bookLocation = googleMap.addMarker(
          MarkerOptions()
            .position(it)
        )
        pickLocationButton.isEnabled = true
      } else {
        bookLocation?.position = it
      }
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
          showExplanation()
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

  private fun showExplanation() {
    Snackbar.make(mapView, string.location_permission_denied_prompt, Snackbar.LENGTH_LONG).show()
  }
}
