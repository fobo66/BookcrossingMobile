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

import androidx.test.platform.app.InstrumentationRegistry
import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Coordinates
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class BookInteractorTest {

  private lateinit var bookInteractor: BookInteractor

  @Before
  fun setUp() {
    val firebaseApp = FirebaseApp.initializeApp(
      InstrumentationRegistry.getInstrumentation().targetContext,
      FirebaseOptions.Builder()
        .setApplicationId("test")
        .setApiKey("apikey")
        .setDatabaseUrl("http://tests.fblocal.com:9000")
        .build(),
      "testapp" + Math.random()
    )

    val database = FirebaseDatabase.getInstance(firebaseApp)
    val auth = FirebaseAuth.getInstance(firebaseApp)

    val authRepository = AuthRepository(auth)
    val booksRepository = BooksRepository(database)

    bookInteractor = BookInteractor(booksRepository, authRepository)
  }

  @Test
  fun releaseBook() {
    bookInteractor.releaseBook(Book())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
  }

  @Test
  fun releaseAcquiredBook() {
    bookInteractor.releaseAcquiredBook("key", "newPosition", "newCity", Coordinates())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
  }
}