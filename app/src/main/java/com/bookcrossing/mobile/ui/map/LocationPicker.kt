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
import androidx.annotation.RequiresPermission
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.string
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE


/**
 * A fragment that shows map for picking book's location as a modal bottom sheet.
 */
class LocationPicker : BottomSheetDialogFragment(), PermissionsListener, OnMapReadyCallback {

  @BindView(R.id.book_location_picker_map)
  lateinit var mapView: MapView

  private lateinit var map: GoogleMap
  private lateinit var unbinder: Unbinder
  private lateinit var permissionsManager: PermissionsManager

  private var bookLocation: Marker? = null

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
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_location_picker, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    unbinder = ButterKnife.bind(this, view)
    permissionsManager = PermissionsManager(this)

    mapView.onCreate(savedInstanceState)

    mapView.getMapAsync(this)
  }

  @SuppressLint("MissingPermission") // permission is checked in PermissionManager
  override fun onMapReady(googleMap: GoogleMap) {
    Timber.d("Map loaded")
    map = googleMap

    if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
      setupCurrentLocation(map)
    } else {
      permissionsManager.requestLocationPermissions(requireActivity())
    }

    map.setOnMapClickListener {
      if (bookLocation == null) {
        bookLocation = map.addMarker(
          MarkerOptions()
            .position(it)
        )
      } else {
        bookLocation?.position = it
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    dialog.behavior.addBottomSheetCallback(bottomSheetCallback)
    return dialog
  }

  override fun onResume() {
    super.onResume()
    mapView.onResume()
  }

  override fun onPause() {
    super.onPause()
    mapView.onPause()
  }

  override fun onStop() {
    super.onStop()
    mapView.onStop()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    mapView.onDestroy()
    unbinder.unbind()
  }

  override fun onCancel(dialog: DialogInterface) {
    (dialog as BottomSheetDialog).behavior.removeBottomSheetCallback(bottomSheetCallback)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>,
    grantResults: IntArray
  ) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  @RequiresPermission(ACCESS_FINE_LOCATION)
  private fun setupCurrentLocation(
    map: GoogleMap
  ) {
    val fusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(requireActivity())

    fusedLocationProviderClient.lastLocation.addOnCompleteListener {
      if (it.isSuccessful) {

        val latLng = it.result?.let {
          LatLng(it.latitude, it.longitude)
        }

        map.isMyLocationEnabled = true

        map.animateCamera(
          CameraUpdateFactory.newLatLngZoom(
            latLng,
            15.0f
          )
        )
      }
    }
  }

  override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
    showExplanation()
  }

  @SuppressLint("MissingPermission") // permission is checked by PermissionManager
  override fun onPermissionResult(granted: Boolean) {
    if (granted) {
      setupCurrentLocation(map)
    } else {
      showExplanation()
      dismiss()
    }
  }

  private fun showExplanation() {
    Snackbar.make(mapView, string.location_permission_denied_prompt, Snackbar.LENGTH_LONG).show()
  }
}
