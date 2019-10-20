/*
 *    Copyright  2019 Andrey Mukamolov
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

package com.bookcrossing.mobile.presenters

import android.net.Uri
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.util.DEFAULT_USER
import com.bookcrossing.mobile.util.EXTRA_CITY
import com.bookcrossing.mobile.util.EXTRA_DEFAULT_CITY
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.FirebaseWrapper
import com.bookcrossing.mobile.util.PACKAGE_NAME
import com.bookcrossing.mobile.util.SystemServicesWrapper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.MvpPresenter
import moxy.MvpView

/**
 * Base class for Moxy presenters. Contains convenience methods shared across all presenters
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {
  private val compositeSubscription = CompositeDisposable()
  protected var firebaseWrapper: FirebaseWrapper
  protected var systemServicesWrapper: SystemServicesWrapper

  private val userId: String
    get() = firebaseWrapper.auth.currentUser?.uid ?: DEFAULT_USER

  protected val city: String?
    get() = systemServicesWrapper.preferences
      .getString(EXTRA_CITY, defaultCity)

  val defaultCity: String?
    get() = systemServicesWrapper.preferences
      .getString(
        EXTRA_DEFAULT_CITY,
        systemServicesWrapper.app.getString(R.string.default_city)
      )

  val isAuthenticated: Boolean
    get() = firebaseWrapper.auth.currentUser != null

  init {
    firebaseWrapper = FirebaseWrapper()
    systemServicesWrapper = SystemServicesWrapper()
    App.getComponent()
      .inject(firebaseWrapper)
    App.getComponent()
      .inject(systemServicesWrapper)
  }

  protected fun unsubscribeOnDestroy(subscription: Disposable) {
    compositeSubscription.add(subscription)
  }

  override fun onDestroy() {
    super.onDestroy()
    compositeSubscription.clear()
  }

  protected fun books(): DatabaseReference {
    return firebaseWrapper.database.getReference("books")
  }

  protected fun stash(): DatabaseReference {
    return firebaseWrapper.database.getReference("stash")
      .child(userId)
  }

  protected fun acquiredBooks(): DatabaseReference {
    return firebaseWrapper.database.getReference("acquiredBooks")
      .child(userId)
  }

  protected fun places(): DatabaseReference {
    return firebaseWrapper.database.getReference("places")
  }

  protected fun placesHistory(key: String): DatabaseReference {
    return firebaseWrapper.database.getReference("placesHistory")
      .child(key)
  }

  fun resolveCover(key: String): StorageReference {
    return firebaseWrapper.storage.getReference("$key.jpg")
  }

  fun buildBookUri(key: String): Uri {
    return Uri.Builder()
      .scheme("bookcrossing")
      .authority(PACKAGE_NAME)
      .path("book")
      .appendQueryParameter(EXTRA_KEY, key)
      .build()
  }
}
