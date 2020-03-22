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
import com.bookcrossing.mobile.models.BookCode.CorrectCode
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.models.Date
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class BookInteractorTest {

  private lateinit var bookInteractor: BookInteractor
  private val authRepository = mockk<AuthRepository>()
  private val booksRepository = mockk<BooksRepository>()

  @Before
  fun setUp() {

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

    every {
      booksRepository.loadBook(any())
    } returns Maybe.just(Book())

    every {
      booksRepository.saveAcquiredBook(any(), any(), any())
    } returns Completable.complete()


    bookInteractor = BookInteractor(booksRepository, authRepository)
  }

  @Test
  fun `release book with empty fields`() {
    bookInteractor.releaseBook(Book())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertError(IllegalStateException::class.java)
      .dispose()
  }

  @Test
  fun `release correct book`() {
    val book = Book().apply {
      name = "test"
      author = "test"
      isFree = true
      city = "test"
      positionName = "test"
      position = Coordinates()
      wentFreeAt = Date()
    }
    bookInteractor.releaseBook(book)
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertComplete()
      .dispose()
  }

  @Test
  fun releaseAcquiredBook() {
    bookInteractor.releaseAcquiredBook("key", "newPosition", "newCity", Coordinates())
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
      .dispose()
  }

  @Test
  fun acquireBook() {
    bookInteractor.acquireBook("key")
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
      .dispose()

    verify {
      booksRepository.saveAcquiredBook(any(), any(), any())
    }

  }

  @Test
  fun checkBook() {
    every {
      booksRepository.checkBook("test")
    } returns Single.just(CorrectCode("test"))

    bookInteractor.checkBook("test")
      .subscribeOn(Schedulers.trampoline())
      .test()
      .assertNoErrors()
      .dispose()
  }
}