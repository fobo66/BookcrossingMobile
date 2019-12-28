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
import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.Single

@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
fun FusedLocationProviderClient.observeLastLocation(): Single<Location> = Single.create { emitter ->
  lastLocation.addOnCompleteListener {
    if (it.isSuccessful) {
      if (!emitter.isDisposed) {
        it.result?.let { location -> emitter.onSuccess(location) } ?: emitter.onError(
          LocationException("Last location unavailable")
        )
      }
    } else {
      if (!emitter.isDisposed) {
        emitter.onError(
          LocationNotLoadedException(
            "Last location retrieving was unsuccessful",
            it.exception
          )
        )
      }
    }
  }
}

class LocationException(message: String) : Exception(message)

class LocationNotLoadedException(message: String, cause: Throwable?) : Exception(message, cause)