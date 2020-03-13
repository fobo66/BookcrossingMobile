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

import com.bookcrossing.mobile.util.ignoreElement
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
  private val firebaseMessaging: FirebaseMessaging
) {

  /** Subscribe user to the notifications about book in stash was acquired or released */
  fun subscribeToBookStashNotifications(key: String): Completable =
    firebaseMessaging.subscribeToTopic(key).ignoreElement()

  /** Unsubscribe user from the notifications about book in stash was acquired or released */
  fun unsubscribeFromBookStashNotifications(key: String): Completable =
    firebaseMessaging.unsubscribeFromTopic(key).ignoreElement()
}