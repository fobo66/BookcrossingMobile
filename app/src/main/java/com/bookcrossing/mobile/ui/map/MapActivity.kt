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
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.presenters.MapPresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.util.EXTRA_COORDINATES
import com.bookcrossing.mobile.util.observe
import com.bookcrossing.mobile.util.onMarkerClicked
import com.github.florent37.runtimepermission.PermissionResult
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.Observable
import moxy.ktx.moxyPresenter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen with all the free books on the map
 */
class MapActivity : BaseActivity(), MvpMapView,
  OnMapReadyCallback, OnInfoWindowClickListener {

  @Inject
  lateinit var presenterProvider: Provider<MapPresenter>

  private val presenter: MapPresenter by moxyPresenter { presenterProvider.get() }

  private lateinit var map: GoogleMap
  private lateinit var permissions: RxPermissions
  private lateinit var locationProvider: FusedLocationProviderClient

  override fun onCreate(savedInstanceState: Bundle?) {
    App.getComponent().inject(this)
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_map)

    permissions = RxPermissions(this)
    locationProvider =
      LocationServices.getFusedLocationProviderClient(this)

    val mapFragment =
      supportFragmentManager.findFragmentById(id.map) as SupportMapFragment?
    mapFragment?.getMapAsync(this)
  }

  override fun onMapReady(googleMap: GoogleMap) {
    map = googleMap
    subscriptions.add(
      requestLocationPermission().subscribe(
        {
          if (it.isAccepted) {
            map.isMyLocationEnabled = true
          }
        }, Timber::e
      )
    )
    map.setOnInfoWindowClickListener(this)

    subscriptions.add(
      map.onMarkerClicked()
        .flatMapMaybe { marker ->
          val key = marker.tag as String
          presenter.loadBookDetails(key)
            .doOnSuccess { book ->
              marker.apply {
                title = book.name
                snippet = book.description
              }
            }
            .map { marker }
        }
        .subscribe { marker ->
          marker.showInfoWindow()
        }
    )

    presenter.loadBooksPositions()

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

  @SuppressLint("MissingPermission") // handled via RxPermission
  private fun requestUserLocation() {
    subscriptions.add(
      requestLocationPermission()
        .flatMapSingle { locationProvider.lastLocation.observe() }
        .subscribe(
          { location: Location ->
            onUserLocationReceived(
              LatLng(
                location.latitude,
                location.longitude
              )
            )
          }, Timber::e
        )
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
    ).apply {
      tag = key
    }
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
    val key = marker.tag as String
    startActivity(BookActivity.getStartIntent(this, key))
  }

  companion object {
    const val DEFAULT_ZOOM_LEVEL = 16.0f

    fun getStartIntent(context: Context, coordinates: Coordinates?): Intent =
      Intent(context, MapActivity::class.java)
        .putExtra(EXTRA_COORDINATES, coordinates)
  }
}