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
import com.bookcrossing.mobile.util.observe
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

  /** Release acquired book */
  fun releaseBook(key: String): Completable {
    book.isFree = true

    return RxFirebaseDatabase.setValue(books().child(key), book)
      .andThen(RxFirebaseDatabase.setValue(places(key), book.position))
      .andThen(
        RxFirebaseDatabase.setValue(
          placesHistory(key).child("${book.city}, ${book.positionName}"),
          book.position
        )
      )
      .andThen(
        acquiredBooks().child(key).removeValue().observe().ignoreElement()
      )
      .subscribeOn(Schedulers.io())
  }
}