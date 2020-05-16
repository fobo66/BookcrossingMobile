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

package com.bookcrossing.mobile.data

import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.BookCode
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.util.ignoreElement
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database.ValueEventListener
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/** Perform actions on books: create, load. etc. */
@Singleton
class BooksRepository @Inject constructor(
  private val booksDataSource: BooksDataSource
) {

  /** Create new book reference in database */
  fun newBook(book: Book): Single<String> = Single.create { emitter ->
    val listener = CompletionListener { error, reference ->
      if (error != null) {
        if (!emitter.isDisposed) {
          emitter.onError(error.toException())
        }
      } else {
        if (!emitter.isDisposed) {
          emitter.onSuccess(reference.key!!) // key is null only for root reference, so it's OK here
        }
      }
    }

    booksDataSource.books().push().setValue(book, listener)
  }

  /** Load book from database */
  fun loadBook(key: String): Maybe<Book> = RxFirebaseDatabase.observeSingleValueEvent(
    booksDataSource.books().child(
      key
    ), Book::class.java
  )

  /** Load books' positions from database */
  fun loadPlaces(): Flowable<LinkedHashMap<String, Coordinates>> =
    RxFirebaseDatabase.observeValueEvent(
      booksDataSource.places(), DataSnapshotMapper.mapOf(
        Coordinates::class.java
      )
    )

  /** Setup new book's position in database */
  fun saveBookPosition(
    key: String,
    city: String?,
    positionName: String?,
    position: Coordinates?
  ): Completable =
    booksDataSource.place(key).setValue(position).ignoreElement()
      .andThen(
        booksDataSource.placesHistory(key).child("$city, $positionName").setValue(
          position
        ).ignoreElement()
      )

  /** Update book's fields in database */
  fun updateBookFields(key: String, bookFields: Map<String, Any>): Completable =
    booksDataSource.books().child(key).updateChildren(bookFields).ignoreElement()

  /** Remove reference to the book from user's acquired books */
  fun removeAcquiredBook(userId: String, key: String): Completable =
    booksDataSource.acquiredBooks(userId).child(key).removeValue()
      .ignoreElement()

  /** Add reference to the book to user's acquired books */
  fun saveAcquiredBook(userId: String, key: String, book: Book): Completable =
    booksDataSource.acquiredBooks(userId).child(key).setValue(book)
      .ignoreElement()

  /** Add reference to the book to user's stash */
  fun addBookToStash(userId: String, key: String): Completable =
    booksDataSource.stash(userId).child(key).setValue(true).ignoreElement()

  /** Remove reference to the book from user's stash */
  fun removeBookFromStash(userId: String, key: String): Completable =
    booksDataSource.stash(userId).child(key).removeValue().ignoreElement()

  /** Load book stash state */
  fun onBookStashed(userId: String, key: String): Single<Boolean> =
    Single.create { emitter ->
      val listener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
          if (!emitter.isDisposed) {
            emitter.onError(error.toException())
          }
        }

        override fun onDataChange(snapshot: DataSnapshot) {
          if (!emitter.isDisposed) {
            emitter.onSuccess(snapshot.exists())
          }
        }
      }

      emitter.setCancellable {
        booksDataSource.stash(userId).child(key)
          .removeEventListener(listener)
      }

      booksDataSource.stash(userId).child(key)
        .addListenerForSingleValueEvent(listener)
    }


  /**
   * Check if given book key exists in the database
   *
   * @param key Key of the book
   */
  fun checkBook(key: String): Single<BookCode> = Single.create { emitter ->
    val listener = object : ValueEventListener {
      override fun onCancelled(error: DatabaseError) {
        if (!emitter.isDisposed) {
          emitter.onError(error.toException())
        }
      }

      override fun onDataChange(snapshot: DataSnapshot) {
        val result = if (!key.isBlank() && snapshot.hasChild(key)) {
          BookCode.CorrectCode(key)
        } else {
          BookCode.IncorrectCode
        }

        if (!emitter.isDisposed) {
          emitter.onSuccess(result)
        }
      }
    }

    emitter.setCancellable {
      booksDataSource.books().removeEventListener(listener)
    }

    booksDataSource.books().addListenerForSingleValueEvent(listener)
  }
}
