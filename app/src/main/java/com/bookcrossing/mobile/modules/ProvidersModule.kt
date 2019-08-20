package com.bookcrossing.mobile.modules

import com.bookcrossing.mobile.util.LocaleProvider
import com.bookcrossing.mobile.util.LocaleProviderImpl
import com.bookcrossing.mobile.util.ResourceProvider
import com.bookcrossing.mobile.util.ResourceProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProvidersModule {
    @Provides
    @Singleton
    fun provideResourceProvider(app: App): ResourceProvider = ResourceProviderImpl(app.applicationContext)

    @Provides
    @Singleton
    fun provideLocaleProvider(app: App): LocaleProvider = LocaleProviderImpl(app.applicationContext.resources)
}