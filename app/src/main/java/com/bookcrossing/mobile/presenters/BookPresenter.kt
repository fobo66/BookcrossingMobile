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
import com.bookcrossing.mobile.util.ignoreElement
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Observable
import moxy.InjectViewState
import timber.log.Timber

@InjectViewState
class BookPresenter : BasePresenter<BookView>() {

  fun subscribeToBookReference(key: String) {
    unsubscribeOnDestroy(
      RxFirebaseDatabase.observeSingleValueEvent(
        books().child(key),
        Book::class.java
      )
        .subscribe({ book -> viewState.onBookLoaded(book) }, { throwable ->
          Timber.e(throwable, "Failed to load book")
          viewState.onErrorToLoadBook()
        })
    )
  }

  fun checkStashingState(key: String) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(stash().child(key))
      .filter { it.exists() }
      .map { it.value as Boolean }
      .subscribe { stashed ->
        updateStashButtonState(stashed)
      })
  }

  fun handleBookStashing(key: String): Observable<Unit> {
    return RxFirebaseDatabase.observeValueEvent(stash().child(key), Boolean::class.java)
      .onErrorReturnItem(false)
      .take(1)
      .doOnNext { stashed -> updateStashButtonState(!stashed) }
      .flatMapCompletable { isStashed ->
        if (isStashed) {
          stash().child(key).removeValue().ignoreElement()
            .andThen(firebaseWrapper.fcm.unsubscribeFromTopic(key).ignoreElement())
            .doOnError {
              Timber.e(it)
              updateStashButtonState(isStashed)
            }
        } else {
          stash().child(key).setValue(!isStashed).ignoreElement()
            .andThen(firebaseWrapper.fcm.subscribeToTopic(key).ignoreElement())
            .doOnError {
              Timber.e(it)
              updateStashButtonState(isStashed)
            }
        }
      }.toObservable<Unit>()
  }

  private fun updateStashButtonState(stashed: Boolean) {
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
    Timber.e("Users complaining to book %s. Consider to check it", key)
    viewState.onAbuseReported()
  }
}
