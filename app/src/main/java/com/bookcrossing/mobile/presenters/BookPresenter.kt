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

import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.interactor.StashInteractor
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.ui.bookpreview.BookView
import com.bookcrossing.mobile.util.BookCoverResolver
import com.google.firebase.database.DatabaseReference
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Observable
import moxy.InjectViewState
import timber.log.Timber
import javax.inject.Inject

@InjectViewState
class BookPresenter @Inject constructor(
  private val booksRepository: BooksRepository,
  private val stashInteractor: StashInteractor,
  val bookCoverResolver: BookCoverResolver
) : BasePresenter<BookView>() {

  /** Load book details */
  fun subscribeToBookReference(key: String) {
    unsubscribeOnDestroy(
      RxFirebaseDatabase.observeSingleValueEvent(
          booksRepository.books().child(key),
          Book::class.java
        )
        .subscribe({ book -> viewState.onBookLoaded(book) }, { throwable ->
          Timber.e(throwable, "Failed to load book")
          viewState.onErrorToLoadBook()
        })
    )
  }

  /** Initial check for stash */
  fun checkStashingState(key: String) {
    unsubscribeOnDestroy(stashInteractor.checkStashedState(key)
      .subscribe { stashed ->
        updateStashButtonState(stashed)
      })
  }

  fun handleBookStashing(key: String): Observable<Unit> {
    return stashInteractor.checkStashedState(key)
      .doOnSuccess { stashed -> updateStashButtonState(!stashed) }
      .flatMapCompletable { isStashed ->
        if (isStashed) {
          stashInteractor.unstashBook(key)
            .doOnError {
              Timber.e(it)
              updateStashButtonState(isStashed)
            }
        } else {
          stashInteractor.stashBook(key)
            .doOnError {
              Timber.e(it)
              updateStashButtonState(isStashed)
            }
        }
      }.toObservable()
  }

  /** Load book's travels */
  fun getPlacesHistory(key: String): DatabaseReference = booksRepository.placesHistory(key)

  /** Send logs for abuse */
  fun reportAbuse(key: String) {
    Timber.e("Users complaining to book %s. Consider to check it", key)
    viewState.onAbuseReported()
  }

  private fun updateStashButtonState(stashed: Boolean) {
    if (stashed) {
      viewState.onBookStashed()
    } else {
      viewState.onBookUnstashed()
    }
  }
}
