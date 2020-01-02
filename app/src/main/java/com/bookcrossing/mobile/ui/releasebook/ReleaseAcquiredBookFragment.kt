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

package com.bookcrossing.mobile.ui.releasebook

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.drawable
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.presenters.ReleaseAcquiredBookPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.MapDelegate
import com.bookcrossing.mobile.util.observe
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.StorageReference
import moxy.presenter.InjectPresenter
import timber.log.Timber

/**
 * Screen for releasing acquired book. User can specify new book location here.
 */
class ReleaseAcquiredBookFragment : BaseFragment(), ReleaseAcquiredBookView, OnMapReadyCallback {

  @BindView(R.id.acquired_book_map)
  lateinit var mapView: MapView
  @BindView(R.id.acquired_book_cover)
  lateinit var cover: ImageView
  @BindView(R.id.acquired_book_author)
  lateinit var authorTextView: TextView

  @BindView(R.id.acquired_book_title)
  lateinit var bookNameTextView: TextView

  @InjectPresenter
  lateinit var presenter: ReleaseAcquiredBookPresenter

  private var bookLocationMarker: Marker? = null

  private lateinit var permissions: RxPermissions
  private lateinit var mapDelegate: MapDelegate
  private lateinit var locationProvider: FusedLocationProviderClient

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_book_release_acquired, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    mapDelegate = MapDelegate(mapView, viewLifecycleOwner)
    permissions = RxPermissions(this)
    locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
    presenter.loadBook(requireArguments().getString(EXTRA_KEY))

    setupCurrentLocation()

    mapView.getMapAsync(this)
  }

  @SuppressLint("MissingPermission")
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

  override fun onLowMemory() {
    super.onLowMemory()
    mapDelegate.onLowMemory()
  }

  override fun showBookDetails(
    book: Book,
    coverUri: StorageReference
  ) {
    GlideApp.with(this)
      .load(coverUri)
      .placeholder(drawable.ic_book_cover_placeholder)
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(cover)

    authorTextView.text = book.author
    bookNameTextView.text = book.name
  }

  override fun onReleased() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onFailedToRelease() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onMapReady(map: GoogleMap) {
    map.setOnMapClickListener {
      if (bookLocationMarker == null) {
        bookLocationMarker = map.addMarker(MarkerOptions().position(it))
      } else {
        bookLocationMarker?.position = it
      }

      presenter.savePosition(it)
    }
  }
}
