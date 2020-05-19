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
import com.bookcrossing.mobile.util.ResourceProvider
import io.reactivex.Single
import javax.inject.Inject

/** Perform operations related to location */
class LocationRepository @Inject constructor(
  private val resourceProvider: ResourceProvider,
  private val locationDataSource: LocationDataSource
) {
  /** Geocode city from coordinates */
  fun resolveCity(latitude: Double, longitude: Double): Single<String> {
    val defaultCity = resourceProvider.getString(R.string.default_city)

    return locationDataSource.resolveCity(latitude, longitude)
      .map { response ->
        response.body()
          ?.features() ?: emptyList()
      }
      .filter { features -> features.isNotEmpty() }
      .map { features -> features[0] }
      .map { feature -> feature.text() ?: defaultCity }
      .switchIfEmpty(Single.just(defaultCity))
      .onErrorReturnItem(defaultCity)
  }
}