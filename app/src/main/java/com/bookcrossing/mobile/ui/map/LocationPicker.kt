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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bookcrossing.mobile.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import timber.log.Timber

/**
 * A fragment that shows map for picking book's location as a modal bottom sheet.
 */
class LocationPicker : BottomSheetDialogFragment(), PermissionsListener {

  @BindView(R.id.book_location_picker_map)
  lateinit var mapView: MapView

  private lateinit var unbinder: Unbinder
  private lateinit var permissionsManager: PermissionsManager

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

    mapView.getMapAsync { map ->
      map.setStyle(Style.MAPBOX_STREETS) { style ->
        Timber.d("Map loaded")

        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
          val locationComponentOptions = LocationComponentOptions.builder(requireContext())
            .build()

          val locationComponentActivationOptions = LocationComponentActivationOptions
            .builder(requireContext(), style)
            .locationComponentOptions(locationComponentOptions)
            .build()

          val locationComponent = map.locationComponent
          locationComponent.activateLocationComponent(locationComponentActivationOptions)
        } else {
          permissionsManager.requestLocationPermissions(requireActivity())
        }
      }
    }
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

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>,
    grantResults: IntArray
  ) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onPermissionResult(granted: Boolean) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
