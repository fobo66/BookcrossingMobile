package com.bookcrossing.mobile.modules;

import android.support.multidex.MultiDexApplication;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.components.AppComponent;
import com.bookcrossing.mobile.components.DaggerAppComponent;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.FirebaseDatabase;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
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
    component = DaggerAppComponent.builder()
        .application(this)
        .prefModule(new PrefModule())
        .apiModule(new ApiModule())
        .locationModule(new LocationModule())
        .build();
    component.inject(this);
  }
}
