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

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.presenters.MapPresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.util.EXTRA_COORDINATES
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.github.florent37.runtimepermission.PermissionResult
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Observable
import moxy.presenter.InjectPresenter
import timber.log.Timber

/**
 * Screen with all the free books on the map
 */
class MapActivity : BaseActivity(), MvpMapView,
  OnMapReadyCallback, OnInfoWindowClickListener {

  @InjectPresenter
  lateinit var presenter: MapPresenter
  private lateinit var map: GoogleMap
  private lateinit var permissions: RxPermissions

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_map)
    permissions = RxPermissions(this)
    val mapFragment =
      supportFragmentManager.findFragmentById(id.map) as SupportMapFragment?
    mapFragment?.getMapAsync(this)
  }

  override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap
    subscriptions.add(
      requestLocationPermission().subscribe(
        {
          map.isMyLocationEnabled = true
        }, Timber::e
      )
    )
    map.setOnInfoWindowClickListener(this)
    presenter.getBooksPositions()

    val requestedZoomPosition: Coordinates? = intent?.getParcelableExtra(EXTRA_COORDINATES)
    if (requestedZoomPosition != null) {
      map.moveCamera(
        CameraUpdateFactory.newLatLngZoom(
          LatLng(
            requestedZoomPosition.lat,
            requestedZoomPosition.lng
          ), DEFAULT_ZOOM_LEVEL
        )
      )
    } else {
      requestUserLocation()
    }
  }

  private fun requestUserLocation() {
    subscriptions.add(
      requestLocationPermission()
        .flatMapSingle { presenter.requestUserLocation() }
        .subscribe(
          { location: Location ->
            onUserLocationReceived(
              LatLng(
                location.latitude,
                location.longitude
              )
            )
          }
        ) { t: Throwable? -> Timber.e(t) }
    )
  }

  private fun requestLocationPermission(): Observable<PermissionResult> {
    return permissions.request(
      permission.ACCESS_FINE_LOCATION,
      permission.ACCESS_COARSE_LOCATION
    )
  }

  override fun onBookMarkerLoaded(
    key: String,
    coordinates: Coordinates
  ) {
    map.addMarker(
      MarkerOptions().position(
        LatLng(
          coordinates.lat,
          coordinates.lng
        )
      )
        .title(key)
        .snippet(presenter.getSnippet(coordinates))
    )
  }

  override fun onErrorToLoadMarker() {
    MaterialDialog(this).show {
      message(string.failed_to_load_books_message)
      title(string.error_dialog_title)
      positiveButton(
        string.ok
      ) { it.dismiss() }
    }
  }

  override fun onUserLocationReceived(coordinates: LatLng) {
    map.animateCamera(
      CameraUpdateFactory.newLatLngZoom(
        coordinates,
        DEFAULT_ZOOM_LEVEL
      )
    )
  }

  override fun onInfoWindowClick(marker: Marker) {
    val intent = Intent(this, BookActivity::class.java)
    intent.putExtra(EXTRA_KEY, presenter.getKey(marker.position))
    startActivity(intent)
  }

  companion object {
    const val DEFAULT_ZOOM_LEVEL = 16.0f

    fun getStartIntent(context: Context, coordinates: Coordinates?): Intent =
      Intent(context, MapActivity::class.java)
        .putExtra(EXTRA_COORDINATES, coordinates)
  }
}