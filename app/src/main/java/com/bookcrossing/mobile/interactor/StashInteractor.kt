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
import com.bookcrossing.mobile.util.ignoreElement
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import javax.inject.Inject
import javax.inject.Singleton

/** Entity for managing stash for book */
@Singleton
class StashInteractor @Inject constructor(
  private val booksRepository: BooksRepository,
  private val authRepository: AuthRepository,
  private val notificationRepository: NotificationRepository
) {

  /** Initial check for stash */
  fun checkStashedState(key: String): Maybe<Boolean> =
    RxFirebaseDatabase.observeSingleValueEvent(
        booksRepository.stash(authRepository.userId).child(
          key
        )
      )
      .map { it.exists() }

  /** Observe stash */
  fun getStashedState(key: String): Flowable<Boolean> =
    RxFirebaseDatabase.observeValueEvent(
        booksRepository.stash(authRepository.userId).child(key),
        Boolean::class.java
      )
      .take(1)

  /** Add book to stash */
  fun stashBook(key: String): Completable =
    booksRepository.stash(authRepository.userId).child(key).setValue(true).ignoreElement()
      .andThen(notificationRepository.subscribeToBookStashNotifications(key))

  /** Remove book from stash */
  fun unstashBook(key: String): Completable =
    booksRepository.stash(authRepository.userId).child(key).removeValue().ignoreElement()
      .andThen(notificationRepository.unsubscribeFromBookStashNotifications(key))
}