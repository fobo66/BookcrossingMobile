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

import androidx.multidex.MultiDexApplication
import com.bookcrossing.mobile.BuildConfig
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.components.AppComponent
import com.bookcrossing.mobile.components.DaggerAppComponent
import com.bookcrossing.mobile.util.CrashlyticsTree
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore.Builder
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
class App : MultiDexApplication(), DaggerComponentProvider {
  override val component: AppComponent by lazy {
    DaggerAppComponent.factory().create(this)
  }

  override fun onCreate() {
    super.onCreate()
    FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    MobileAds.initialize(this, resources.getString(string.admob_app_id))
    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    } else {
      Timber.plant(CrashlyticsTree())
    }
    val crashlyticsKit = Crashlytics.Builder().core(
      Builder().disabled(BuildConfig.DEBUG).build()
    ).build()
    Fabric.with(this, crashlyticsKit)
  }
}