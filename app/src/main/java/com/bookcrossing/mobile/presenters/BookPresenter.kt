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

package com.bookcrossing.mobile.presenters

import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.ui.bookpreview.BookView
import com.crashlytics.android.Crashlytics
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.RxFirebaseDatabase
import moxy.InjectViewState
import timber.log.Timber

@InjectViewState
class BookPresenter : BasePresenter<BookView>() {

  private var stashed = false

  fun subscribeToBookReference(key: String) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book::class.java)
      .subscribe({ book -> viewState.onBookLoaded(book) }, { throwable ->
        Timber.e(throwable, "Failed to load book")
        viewState.onErrorToLoadBook()
      }))
  }

  fun checkStashingState(key: String) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(stash().child(key))
      .filter { it.exists() }
      .subscribe { data ->
        stashed = data.value as Boolean
        updateStashButtonState()
      })
  }

  fun handleBookStashing(key: String) {
    stashed = !stashed
    if (stashed) {
      stash().child(key).setValue(stashed)
      firebaseWrapper.fcm.subscribeToTopic(key)
    } else {
      stash().child(key).removeValue()
      firebaseWrapper.fcm.unsubscribeFromTopic(key)
    }
    updateStashButtonState()
  }

  private fun updateStashButtonState() {
    if (stashed) {
      viewState.onBookStashed()
    } else {
      viewState.onBookUnstashed()
    }
  }

  fun getPlacesHistory(key: String): DatabaseReference {
    return placesHistory(key)
  }

  fun reportAbuse(key: String) {
    Crashlytics.log(String.format("Users complaining to book %s. Consider to check it", key))
    viewState.onAbuseReported()
  }
}
