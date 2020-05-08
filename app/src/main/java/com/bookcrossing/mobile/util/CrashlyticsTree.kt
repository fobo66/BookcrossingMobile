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

package com.bookcrossing.mobile.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Log exceptions to Crashlytics
 * */
class CrashlyticsTree(private val crashlytics: FirebaseCrashlytics) : Timber.Tree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    crashlytics.log(message)
    if (t != null) {
      crashlytics.recordException(t)
    }
  }
}
