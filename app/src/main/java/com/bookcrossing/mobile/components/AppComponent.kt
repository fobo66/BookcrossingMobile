package com.bookcrossing.mobile.components

import com.bookcrossing.mobile.modules.ApiModule
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.modules.LocationModule
import com.bookcrossing.mobile.modules.PrefModule
import com.bookcrossing.mobile.presenters.MainPresenter
import com.bookcrossing.mobile.util.FirebaseWrapper
import com.bookcrossing.mobile.util.SystemServicesWrapper
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
@Singleton
@Component(modules = [PrefModule::class, LocationModule::class, ApiModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: App): Builder

        fun prefModule(prefModule: PrefModule): Builder

        fun apiModule(apiModule: ApiModule): Builder

        fun locationModule(locationModule: LocationModule): Builder

        fun build(): AppComponent
    }

    fun inject(app: App)

    fun inject(firebaseWrapper: FirebaseWrapper)

    fun inject(systemServicesWrapper: SystemServicesWrapper)

    fun inject(presenter: MainPresenter)
}
