package com.bookcrossing.mobile.modules;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Created by fobo66 on 15.11.2016.
 */

@Module public class AppModule {

  private App app;

  AppModule(App app) {
    this.app = app;
  }

  @Provides @Singleton App provideApp() {
    return app;
  }
}
