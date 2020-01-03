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
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState


/**
 * Presenter for release acquired book screen
 */
@InjectViewState
class ReleaseAcquiredBookPresenter : BasePresenter<ReleaseAcquiredBookView>() {

  private lateinit var book: Book

  private val validator =
    InputValidator(NotEmptyRule(), LengthRule(maxLength = 100))

  /** Load book details */
  fun loadBook(key: String?) {
    if (!key.isNullOrEmpty()) {
      unsubscribeOnDestroy(
        RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book::class.java)
          .subscribe {
            book = it
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
  fun releaseBook(key: String, newCity: String, newPositionName: String): Completable {
    book.apply {
      isFree = true
      city = newCity
      positionName = newPositionName
    }

    return books().child(key).setValue(book).ignoreElement()
      .andThen(places(key).setValue(book.position).ignoreElement())
      .andThen(
        placesHistory(key).child("${book.city}, ${book.positionName}")
          .setValue(book.position)
          .ignoreElement()
      )
      .andThen(
        acquiredBooks().child(key).removeValue().ignoreElement()
      )
      .subscribeOn(Schedulers.io())
  }
}