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

package com.bookcrossing.mobile.location

import android.Manifest
import android.location.Location
import androidx.annotation.RequiresPermission
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.util.LocaleProvider
import com.bookcrossing.mobile.util.ResourceProvider
import com.bookcrossing.mobile.util.observeLastLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.geojson.Point.fromLngLat
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LocationRepository @Inject constructor(
  private val fusedLocationProviderClient: FusedLocationProviderClient,
  private val resourceProvider: ResourceProvider,
  private val localeProvider: LocaleProvider
) {

  /** Load last location of the device. Throws error if location is null */
  @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
  fun getLastKnownUserLocation(): Single<Location> {
    return fusedLocationProviderClient.observeLastLocation()
  }

  /** Geocode city from coordinates */
  fun resolveUserCity(latitude: Double, longitude: Double): Single<String> {
    val reverseGeocodeRequest = MapboxGeocoding.builder()
      .accessToken(resourceProvider.getString(R.string.mapbox_access_token))
      .languages(localeProvider.currentLocale.language)
      .limit(1)
      .query(fromLngLat(longitude, latitude))
      .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
      .build()

    val defaultCity = resourceProvider.getString(R.string.default_city)

    return Single.fromCallable { reverseGeocodeRequest.executeCall() }
      .map { response ->
        response.body()
          ?.features() ?: emptyList()
      }
      .filter { features -> features.isNotEmpty() }
      .map { features -> features[0] }
      .map { feature -> feature.text() ?: defaultCity }
      .switchIfEmpty(Single.just(defaultCity))
      .onErrorReturnItem(defaultCity)
      .subscribeOn(Schedulers.io())
  }
}
