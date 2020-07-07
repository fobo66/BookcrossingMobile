/*
 *    Copyright 2020 Andrey Mukamolov
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

package com.bookcrossing.mobile.interactor

import com.bookcrossing.mobile.data.LocationRepository
import com.bookcrossing.mobile.models.Coordinates
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class LocationInteractor @Inject constructor(
  private val locationRepository: LocationRepository
) {

  /**
   * Determine the city of the location of the book
   */
  fun resolveCity(coordinates: Coordinates?): Single<String> {
    return locationRepository.resolveCity(
      coordinates?.lat ?: 0.0,
      coordinates?.lng ?: 0.0
    )
      .doOnError(Timber::e)
  }
}