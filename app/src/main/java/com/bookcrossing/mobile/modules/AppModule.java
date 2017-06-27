package com.bookcrossing.mobile.modules;

import dagger.Module;

@Module public class AppModule {

  private App app;

  AppModule(App app) {
    this.app = app;
  }
}
