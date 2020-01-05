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
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.ui.releasebook.ReleaseAcquiredBookView
import com.bookcrossing.mobile.util.InputValidator
import com.bookcrossing.mobile.util.LengthRule
import com.bookcrossing.mobile.util.NotEmptyRule
import com.bookcrossing.mobile.util.ValidationResult
import com.bookcrossing.mobile.util.ignoreElement
import com.google.android.gms.maps.model.LatLng
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import timber.log.Timber


/**
 * Presenter for release acquired book screen
 */
@InjectViewState
class ReleaseAcquiredBookPresenter : BasePresenter<ReleaseAcquiredBookView>() {

  private lateinit var book: Book
  private lateinit var key: String

  private val validator =
    InputValidator(NotEmptyRule(), LengthRule(maxLength = 100))

  /** Load book details */
  fun loadBook(key: String?) {
    if (!key.isNullOrEmpty()) {
      unsubscribeOnDestroy(
        RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book::class.java)
          .subscribe {
            book = it
            this.key = key
            viewState.showBookDetails(it, resolveCover(key))
          }
      )
    }
  }

  /** Save selected position of the book */
  fun savePosition(bookPosition: LatLng) {
    book.position = Coordinates(bookPosition)
  }

  /**
   * Validate user's input
   */
  fun validateInput(input: CharSequence): ValidationResult = validator.validate(input.toString())


  /** Release acquired book */
  fun releaseBook(newPositionName: String): Completable {
    return systemServicesWrapper.locationRepository.resolveCity(
      book.position.lat,
      book.position.lng
    )
      .doOnSuccess { newCity ->
        book.apply {
          isFree = true
          city = newCity
          positionName = newPositionName
        }
      }
      .flatMapCompletable {
        books().child(key).setValue(book).ignoreElement()
      }
      .andThen(places(key).setValue(book.position).ignoreElement())
      .andThen(
        placesHistory(key).child("${book.city}, $newPositionName")
          .setValue(book.position)
          .ignoreElement()
      )
      .andThen(
        acquiredBooks().child(key).removeValue().ignoreElement()
      )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnComplete { viewState.onReleased() }
      .doOnError {
        Timber.e(it, "Failed to release book")
        viewState.onFailedToRelease()
      }
  }
}