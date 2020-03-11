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
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.util.ignoreElement
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/** Wrapper around Firebase database references */
@Singleton
class BooksRepository @Inject constructor(
  private val database: FirebaseDatabase
) {

  /** Load all books */
  fun books(): DatabaseReference {
    return database.getReference("books")
  }

  /** Load books stashed by the user */
  fun stash(userId: String): DatabaseReference {
    return database.getReference("stash")
      .child(userId)
  }

  /** Load books acquired by the user */
  fun acquiredBooks(userId: String): DatabaseReference {
    return database.getReference("acquiredBooks")
      .child(userId)
  }

  /** Load all books' current positions */
  fun places(): DatabaseReference {
    return database.getReference("places")
  }

  /** Load given book's current position */
  fun place(key: String): DatabaseReference {
    return database.getReference("places")
      .child(key)
  }

  /** Load given book's positions history */
  fun placesHistory(key: String): DatabaseReference {
    return database.getReference("placesHistory")
      .child(key)
  }

  /** Create new book reference in database */
  fun newBook(book: Book): Single<String> = Single.create<String> { emitter ->
    val listener: CompletionListener = object : CompletionListener {
      override fun onComplete(error: DatabaseError?, reference: DatabaseReference) {
        if (error != null) {
          if (!emitter.isDisposed) {
            emitter.onError(error.toException())
          }
        } else {
          if (!emitter.isDisposed) {
            emitter.onSuccess(reference.key!!)
          }
        }
      }
    }

    books().push().setValue(book, listener)
  }

  /** Setup new book's position in database */
  fun saveBookPosition(
    key: String,
    city: String,
    positionName: String,
    position: Coordinates
  ): Completable =
    place(key).setValue(position).ignoreElement()
      .andThen(
        placesHistory(key).child("$city, $positionName").setValue(
          position
        ).ignoreElement()
      )

  /** Update book's fields in database */
  fun updateBookFields(key: String, bookFields: Map<String, Any>): Completable =
    books().child(key).updateChildren(bookFields).ignoreElement()
}