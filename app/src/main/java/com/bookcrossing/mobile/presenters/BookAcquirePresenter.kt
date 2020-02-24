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

import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.BookCode
import com.bookcrossing.mobile.ui.acquire.BookAcquireView
import com.bookcrossing.mobile.util.ignoreElement
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Maybe
import moxy.InjectViewState
import javax.inject.Inject

@InjectViewState
class BookAcquirePresenter @Inject constructor(
  private val booksRepository: BooksRepository,
  private val authRepository: AuthRepository
) : BasePresenter<BookAcquireView>() {

  fun handleAcquisitionResult(code: BookCode) {
    when (code) {
      is BookCode.CorrectCode -> viewState.onAcquired()
      is BookCode.IncorrectCode -> viewState.onIncorrectKey()
    }
  }

  fun processBookAcquisition(key: String): Completable {
    return booksRepository.books().child(key).child("free").setValue(false).ignoreElement()
      .andThen(
        RxFirebaseDatabase.observeSingleValueEvent(
          booksRepository.books().child(key),
          Book::class.java
        )
      )
      .flatMapCompletable { book ->
        booksRepository.acquiredBooks(authRepository.userId).child(key).setValue(book)
          .ignoreElement()
      }
  }

  fun validateCode(key: String): Maybe<BookCode> {
    return RxFirebaseDatabase.observeSingleValueEvent(booksRepository.books())
      .flatMap { dataSnapshot ->
        if (!key.isBlank() && dataSnapshot.hasChild(key)) {
          return@flatMap Maybe.just(BookCode.CorrectCode(key))
        }

        return@flatMap Maybe.just(BookCode.IncorrectCode)
      }
  }
}
