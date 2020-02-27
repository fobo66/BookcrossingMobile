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

package com.bookcrossing.mobile.interactor

import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.util.ignoreElement
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/** Class for managing books: setting them free and acquiring them */
@Singleton
class BookInteractor @Inject constructor(
  private val booksRepository: BooksRepository,
  private val authRepository: AuthRepository
) {

  /** Release new book */
  fun releaseBook(book: Book): Completable =
    Single.fromCallable { booksRepository.books().push() }
      .flatMap { newBookReference ->
        newBookReference.setValue(book).ignoreElement()
          .andThen(
            if (newBookReference.key != null) Single.just(newBookReference.key) else Single.error(
              IllegalStateException("Book was not properly released")
            )
          )
      }
      .flatMapCompletable { key: String ->
        saveBookPosition(key, book.city, book.positionName, book.position)
      }

  /**
   * Release acquired book
   *
   * @param key Key of the book
   * @param newPositionName New name of the place where user left the book
   * @param newCity New city where book is located
   * @param newPosition New coordinates of the book
   */
  fun releaseAcquiredBook(
    key: String,
    newPositionName: String,
    newCity: String,
    newPosition: Coordinates
  ): Completable =
    booksRepository.books().child(key).child("free").setValue(true).ignoreElement()
      .andThen(booksRepository.books().child(key).child("city").setValue(newCity).ignoreElement())
      .andThen(booksRepository.books().child(key).child("positionName").setValue(newPositionName).ignoreElement())
      .andThen(booksRepository.books().child(key).child("position").setValue(newPosition).ignoreElement())
      .andThen(saveBookPosition(key, newCity, newPositionName, newPosition))
      .andThen(
        booksRepository.acquiredBooks(authRepository.userId).child(key).removeValue().ignoreElement()
      )

  private fun saveBookPosition(
    key: String,
    city: String,
    positionName: String,
    position: Coordinates
  ): Completable =
    booksRepository.place(key).setValue(position).ignoreElement()
      .andThen(
        booksRepository.placesHistory(key).child("$city, $positionName").setValue(
          position
        ).ignoreElement()
      )
}