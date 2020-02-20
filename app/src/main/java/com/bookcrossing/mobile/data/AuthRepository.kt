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

import android.net.Uri
import com.bookcrossing.mobile.util.DEFAULT_USER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

/** Repository to handle auth */
@Singleton
class AuthRepository @Inject constructor(
  private val auth: FirebaseAuth
) {

  /** Id of the currently signed in user */
  val userId: String
    get() = auth.currentUser?.uid ?: DEFAULT_USER

  /** Profile photo URI of the currently signed in user */
  val photoUrl: Uri?
    get() = auth.currentUser?.photoUrl

  /** Check if user is authenticated */
  val isAuthenticated: Boolean
    get() = auth.currentUser != null

  /** Observe auth state changes */
  fun onAuthenticated() = Observable.create<FirebaseAuth> { emitter ->
    val authStateListener = AuthStateListener { firebaseAuth ->
      if (!emitter.isDisposed) {
        emitter.onNext(firebaseAuth)
      }
    }
    auth.addAuthStateListener(authStateListener)
    emitter.setCancellable { auth.removeAuthStateListener(authStateListener) }
  }
}