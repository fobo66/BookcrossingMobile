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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.MapPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.util.MapDelegate
import com.bookcrossing.mobile.util.observe
import com.bookcrossing.mobile.util.onMarkerClicked
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import moxy.ktx.moxyPresenter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen to display books' locations on map
 */
class MapsFragment : BaseFragment(), MvpMapView, OnInfoWindowClickListener {

  @BindView(R.id.books_map)
  lateinit var mapView: MapView

  @Inject
  lateinit var presenterProvider: Provider<MapPresenter>

  private val presenter: MapPresenter by moxyPresenter { presenterProvider.get() }

  private lateinit var map: GoogleMap
  private lateinit var mapDelegate: MapDelegate
  private lateinit var permissions: RxPermissions
  private lateinit var locationProvider: FusedLocationProviderClient


  private val mapCallback = OnMapReadyCallback { googleMap ->
    map = googleMap

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
  }

  override fun onAttach(context: Context) {
    injector.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_maps, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    locationProvider =
      LocationServices.getFusedLocationProviderClient(requireActivity())
    permissions = RxPermissions(this)
    mapDelegate = MapDelegate(mapView, viewLifecycleOwner)

    setupCurrentLocation()

    mapView.getMapAsync(mapCallback)
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
    MaterialDialog(requireContext()).show {
      message(string.failed_to_load_books_message)
      title(string.error_dialog_title)
      positiveButton(string.ok) { it.dismiss() }
    }
  }

  override fun onInfoWindowClick(marker: Marker) {
    val key = marker.tag as String
    startActivity(BookActivity.getStartIntent(requireContext(), key))
  }

  @SuppressLint("MissingPermission") // permission is checked in RxPermission
  private fun setupCurrentLocation() {
    subscriptions.add(
      permissions.request(permission.ACCESS_FINE_LOCATION)
        .flatMapSingle {
          locationProvider.lastLocation.observe()
        }
        .map { LatLng(it.latitude, it.longitude) }
        .subscribe({
          mapDelegate.setupCurrentLocation(it)
        }, Timber::e)
    )
  }
}