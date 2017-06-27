package com.bookcrossing.mobile.modules;

import dagger.Module;
import dagger.Provides;
import io.nlopez.smartlocation.SmartLocation;
import javax.inject.Singleton;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 6/25/17.
 */

@Module public class LocationModule {
  @Provides @Singleton public SmartLocation provideSmartLocation(App app) {
    return SmartLocation.with(app.getApplicationContext());
  }
}
