/*
 *     Copyright 2019 Andrey Mukamolov
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.bookcrossing.mobile.presenters

import android.net.Uri
import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.ui.profile.ProfileView
import com.google.firebase.database.DatabaseReference
import moxy.InjectViewState
import javax.inject.Inject

/**
 * Created by fobo66 on 08.05.17.
 */

@InjectViewState
class ProfilePresenter @Inject constructor(
  private val booksRepository: BooksRepository,
  private val authRepository: AuthRepository
) : BasePresenter<ProfileView>() {

  val acquiredBooks: DatabaseReference
    get() = booksRepository.acquiredBooks(authRepository.userId)

  val photoUrl: Uri?
    get() = authRepository.photoUrl

  val isAuthenticated: Boolean
    get() = authRepository.isAuthenticated
}
