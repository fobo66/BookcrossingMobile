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
import javax.inject.Inject
import javax.inject.Singleton

/** Exposes Firebase Database references to the presenters in case when it's needed for FirebaseUI */
@Singleton
class BooksReferencesRepository @Inject constructor(
  private val booksDataSource: BooksDataSource
) {

  /** Load all books */
  fun books(): DatabaseReference = booksDataSource.books()

  /** Load books stashed by the user */
  fun stash(userId: String): DatabaseReference = booksDataSource.stash(userId)

  /** Load books acquired by the user */
  fun acquiredBooks(userId: String): DatabaseReference = booksDataSource.acquiredBooks(userId)

  /** Load all books' current positions reference */
  fun places(): DatabaseReference = booksDataSource.places()

  /** Load given book's current position */
  fun place(key: String): DatabaseReference = booksDataSource.place(key)

  /** Load given book's positions history */
  fun placesHistory(key: String): DatabaseReference = booksDataSource.placesHistory(key)
}