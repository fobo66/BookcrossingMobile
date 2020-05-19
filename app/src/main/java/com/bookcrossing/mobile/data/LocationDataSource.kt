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

package com.bookcrossing.mobile.data

import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.util.LocaleProvider
import com.bookcrossing.mobile.util.ResourceProvider
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point.fromLngLat
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

/** Perform operations related to location */
class LocationDataSource @Inject constructor(
  private val resourceProvider: ResourceProvider,
  private val localeProvider: LocaleProvider
) {
  /** Geocode city from coordinates */
  fun resolveCity(latitude: Double, longitude: Double): Single<Response<GeocodingResponse>> {
    val reverseGeocodeRequest = MapboxGeocoding.builder()
      .accessToken(resourceProvider.getString(R.string.mapbox_access_token))
      .languages(localeProvider.currentLocale.language)
      .limit(1)
      .query(fromLngLat(longitude, latitude))
      .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
      .build()

    return Single.fromCallable { reverseGeocodeRequest.executeCall() }
      .subscribeOn(Schedulers.io())
  }
}