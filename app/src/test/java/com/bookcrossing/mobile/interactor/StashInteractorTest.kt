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
import com.bookcrossing.mobile.data.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class StashInteractorTest {

  private lateinit var stashInteractor: StashInteractor
  private val authRepository: AuthRepository = mockk()
  private val booksRepository: BooksRepository = mockk()
  private val notificationRepository: NotificationRepository = mockk()

  @Before
  fun setUp() {
    every {
      authRepository.userId
    } returns "test"

    every {
      booksRepository.addBookToStash(any(), any())
    } returns Completable.complete()

    every {
      booksRepository.removeBookFromStash(any(), any())
    } returns Completable.complete()

    every {
      notificationRepository.subscribeToBookStashNotifications(any())
    } returns Completable.complete()

    every {
      notificationRepository.unsubscribeFromBookStashNotifications(any())
    } returns Completable.complete()



    stashInteractor = StashInteractor(booksRepository, authRepository, notificationRepository)
  }

  @Test
  fun `check existing stashed state`() {
    every {
      booksRepository.onBookStashed(any(), any())
    } returns Single.just(true)

    stashInteractor.checkStashedState("test")
      .test()
      .assertValue(true)
      .assertComplete()
  }

  @Test
  fun `check missing stashed state`() {
    every {
      booksRepository.onBookStashed(any(), any())
    } returns Single.just(false)

    stashInteractor.checkStashedState("test")
      .test()
      .assertValue(false)
      .assertComplete()
  }

  @Test
  fun `error during checking stashed state`() {
    every {
      booksRepository.onBookStashed(any(), any())
    } returns Single.error(Exception())

    stashInteractor.checkStashedState("test")
      .test()
      .assertValue(false)
      .assertNoErrors()
  }

  @Test
  fun stashBook() {
    stashInteractor.stashBook("test")
      .test()
      .assertComplete()
  }

  @Test
  fun unstashBook() {
    stashInteractor.unstashBook("test")
      .test()
      .assertComplete()
  }
}