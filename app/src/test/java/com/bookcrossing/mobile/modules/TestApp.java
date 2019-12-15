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

import com.google.firebase.FirebaseApp;

/**
 * Mock class to make Robolectric tests start correctly
 *
 * (c) 2019 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 2019-08-31.
 */
public class TestApp extends App {

  @Override public void onCreate() {
    FirebaseApp.initializeApp(this);
    super.onCreate();
  }
}
