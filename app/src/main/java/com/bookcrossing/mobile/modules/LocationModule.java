package com.bookcrossing.mobile.modules;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.nlopez.smartlocation.SmartLocation;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 6/25/17.
 */

@Module public class LocationModule {
  @Provides @Singleton public SmartLocation provideSmartLocation(App app) {
    return SmartLocation.with(app.getApplicationContext());
  }

  @Provides
  @Singleton
  public FusedLocationProviderClient provideFusedLocationProviderClient(App app) {
    return LocationServices.getFusedLocationProviderClient(app.getApplicationContext());
  }
}
