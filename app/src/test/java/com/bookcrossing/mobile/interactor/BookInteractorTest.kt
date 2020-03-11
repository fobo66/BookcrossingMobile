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
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class BookInteractorTest {

  private lateinit var bookInteractor: BookInteractor

  @Before
  fun setUp() {
    val authRepository = mockk<AuthRepository>()
    val booksRepository = mockk<BooksRepository>()

    every {
      authRepository.userId
    } returns "test"

    every {
      booksRepository.saveBookPosition(any(), any(), any(), any())
    } returns Completable.complete()

    every {
      booksRepository.updateBookFields(any(), any())
    } returns Completable.complete()

    every {
      booksRepository.removeAcquiredBook(any(), any())
    } returns Completable.complete()

    every {
      booksRepository.newBook(any())
    } returns Single.just("test")

    bookInteractor = BookInteractor(booksRepository, authRepository)
  }

  @Test
  fun `release book with empty fields`() {
    bookInteractor.releaseBook(Book())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertError(IllegalStateException::class.java)
  }

  @Test
  fun releaseAcquiredBook() {
    bookInteractor.releaseAcquiredBook("key", "newPosition", "newCity", Coordinates())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
  }
}