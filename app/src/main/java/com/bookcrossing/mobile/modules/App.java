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

package com.bookcrossing.mobile.modules;

import androidx.multidex.MultiDexApplication;

import com.bookcrossing.mobile.BuildConfig;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.components.AppComponent;
import com.bookcrossing.mobile.components.DaggerAppComponent;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;

import timber.log.Timber;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */

public class App extends MultiDexApplication {

  public static AppComponent getComponent() {
    return component;
  }

  private static AppComponent component;

  @Override public void onCreate() {
    super.onCreate();
    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    RxPaparazzo.register(this);
    MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }

    component = DaggerAppComponent.builder()
        .application(this)
        .prefModule(new PrefModule())
        .apiModule(new ApiModule())
        .locationModule(new LocationModule())
        .build();
    component.inject(this);
  }
}
