package com.bookcrossing.mobile.util

import android.content.SharedPreferences

import com.bookcrossing.mobile.location.LocationRepository
import com.bookcrossing.mobile.modules.App
import dagger.Lazy
import javax.inject.Inject

/**
 * Created by fobo66 on 25.4.17.
 */

class SystemServicesWrapper {

    @Inject
    lateinit var preferencesLazy: Lazy<SharedPreferences>
    @Inject
    lateinit var appLazy: Lazy<App>
    @Inject
    lateinit var locationRepositoryLazy: Lazy<LocationRepository>


    val preferences: SharedPreferences
        get() = preferencesLazy.get()

    val app: App
        get() = appLazy.get()


    val locationRepository: LocationRepository
        get() = locationRepositoryLazy.get()
}
