package com.bookcrossing.mobile.modules;

import com.google.firebase.FirebaseApp;

/**
 * Mock class to make Robolectric tests start correctly
 *
 * (c) 2019 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 2019-08-31.
 */
public class TestApp extends App {

  @Override public void onCreate() {
    FirebaseApp.initializeApp(this);
    super.onCreate();
  }
}
