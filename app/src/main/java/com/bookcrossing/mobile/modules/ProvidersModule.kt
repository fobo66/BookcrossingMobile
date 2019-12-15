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
