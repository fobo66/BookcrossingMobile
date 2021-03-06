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

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

/** Load data about books and related stuff from Firebase */
@Singleton
class BooksDataSource @Inject constructor(
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
    return places()
      .child(key)
  }

  /** Load given book's positions history */
  fun placesHistory(key: String): DatabaseReference {
    return database.getReference("placesHistory")
      .child(key)
  }
}