package com.bookcrossing.mobile.util;

import android.content.SharedPreferences;
import com.bookcrossing.mobile.modules.App;
import dagger.Lazy;
import io.nlopez.smartlocation.SmartLocation;
import javax.inject.Inject;

/**
 * Created by fobo66 on 25.4.17.
 */

public class SystemServicesWrapper {
  @Inject Lazy<SharedPreferences> preferencesLazy;

  @Inject Lazy<App> appLazy;

  @Inject Lazy<SmartLocation> locationLazy;

  public SharedPreferences getPreferences() {
    return preferencesLazy.get();
  }

  public App getApp() {
    return appLazy.get();
  }

  public SmartLocation getLocation() {
    return locationLazy.get();
  }
}
