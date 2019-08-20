package com.bookcrossing.mobile.modules

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 6/25/17.
 */

@Module
class LocationModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: App): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app.applicationContext)
    }
}
