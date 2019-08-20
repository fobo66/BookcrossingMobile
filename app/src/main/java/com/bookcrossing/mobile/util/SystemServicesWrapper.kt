package com.bookcrossing.mobile.util

import android.content.SharedPreferences

import com.bookcrossing.mobile.location.LocationRepository
import com.bookcrossing.mobile.modules.App
import dagger.Lazy
import io.nlopez.smartlocation.SmartLocation
import javax.inject.Inject

/**
 * Created by fobo66 on 25.4.17.
 */

class SystemServicesWrapper @Inject constructor(
        private val preferencesLazy: Lazy<SharedPreferences>,
        private val appLazy: Lazy<App>,
        private val locationLazy: Lazy<SmartLocation>,
        private val locationRepositoryLazy: Lazy<LocationRepository>
) {


    val preferences: SharedPreferences
        get() = preferencesLazy.get()

    val app: App
        get() = appLazy.get()

    val location: SmartLocation
        get() = locationLazy.get()

    val locationRepository: LocationRepository
        get() = locationRepositoryLazy.get()
}
