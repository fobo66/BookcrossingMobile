package com.bookcrossing.mobile.modules;

import android.support.multidex.MultiDexApplication;
import com.bookcrossing.mobile.components.AppComponent;
import com.bookcrossing.mobile.components.DaggerAppComponent;
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
    component = DaggerAppComponent.builder()
        .appModule(new AppModule(this))
        .prefModule(new PrefModule())
        .apiModule(new ApiModule())
        .build();
  }
}
