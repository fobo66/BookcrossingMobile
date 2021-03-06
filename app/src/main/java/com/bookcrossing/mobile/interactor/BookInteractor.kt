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
import com.bookcrossing.mobile.models.BookCode
import com.bookcrossing.mobile.models.Coordinates
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
  fun releaseBook(book: Book): Single<String> {
    check(!book.isEmpty()) { "Book should not have empty fields" }

    return booksRepository.newBook(book)
      .flatMap { key: String ->
        booksRepository.saveBookPosition(key, book.city, book.positionName, book.position)
          .andThen(Single.just(key))
      }
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
    newPosition: Coordinates?
  ): Completable {

    val bookDataToUpdate: MutableMap<String, Any> = mutableMapOf(
      "free" to true,
      "city" to newCity,
      "positionName" to newPositionName
    )

    if (newPosition != null) {
      bookDataToUpdate["position"] = newPosition
    }

    return booksRepository.updateBookFields(key, bookDataToUpdate)
      .andThen(booksRepository.saveBookPosition(key, newCity, newPositionName, newPosition))
      .andThen(
        booksRepository.removeAcquiredBook(authRepository.userId, key)
      )
  }

  /**
   * Performs actions needed to indicate that user has acquired given book
   *
   * @param key Key of the book
   */
  fun acquireBook(key: String): Completable {
    val bookDataToUpdate = mapOf(
      "free" to false
    )

    return booksRepository.updateBookFields(key, bookDataToUpdate)
      .andThen(
        booksRepository.loadBook(key)
      )
      .flatMapCompletable { book ->
        booksRepository.saveAcquiredBook(authRepository.userId, key, book)
      }
  }

  /**
   * Check if given book key is correct
   *
   * @param key Key of the book
   */
  fun checkBook(key: String): Single<BookCode> = booksRepository.checkBook(key)
}

private fun Book.isEmpty(): Boolean {
  return listOf(name, author, description, positionName, city).any { it.isNullOrBlank() }
}