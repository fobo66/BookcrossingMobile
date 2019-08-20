package com.bookcrossing.mobile.location

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import io.reactivex.Single
import javax.inject.Inject

class LocationRepository @Inject constructor(
        private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    fun getLastKnownUserLocation(): Single<Location?> {
        return Single.create { emitter ->
            fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { emitter.onSuccess(it) }
                    .addOnFailureListener { emitter.onError(it) }
        }
    }
}