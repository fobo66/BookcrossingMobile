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

package com.bookcrossing.mobile.util

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

/**
 * Handle all the common cases with the map independent of the layout
 */
class MapDelegate(private var mapView: MapView?, lifecycleOwner: LifecycleOwner) :
  LifecycleEventObserver {

  init {
    lifecycleOwner.lifecycle.addObserver(this)
  }

  override fun onStateChanged(source: LifecycleOwner, event: Event) {
    when (event) {
      ON_CREATE -> mapView?.onCreate(null)
      ON_START -> mapView?.onStart()
      ON_RESUME -> mapView?.onResume()
      ON_PAUSE -> mapView?.onPause()
      ON_STOP -> mapView?.onStop()
      ON_DESTROY -> {
        mapView?.onDestroy()
        mapView = null
        source.lifecycle.removeObserver(this)
      }
      else -> Timber.d("Lifecycle event happened: %s", event)
    }
  }

  /** Handle onLowMemory*/
  fun onLowMemory() {
    mapView?.onLowMemory()
  }

  @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  fun setupCurrentLocation(currentLocation: LatLng, zoom: Float = 15.0f) {
    mapView?.getMapAsync { map ->
      map.isMyLocationEnabled = true

      map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
          currentLocation,
          zoom
        )
      )
    }
  }
}