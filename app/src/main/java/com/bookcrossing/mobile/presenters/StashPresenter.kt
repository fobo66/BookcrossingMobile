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

package com.bookcrossing.mobile.presenters

import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.BooksRepository
import com.bookcrossing.mobile.ui.stash.StashView
import com.google.firebase.database.DatabaseReference
import moxy.InjectViewState
import javax.inject.Inject

/**
 * Presenter fo stash screen
 */
@InjectViewState
class StashPresenter @Inject constructor(
  private val booksRepository: BooksRepository,
  private val authRepository: AuthRepository
) : BasePresenter<StashView>() {

  val stashedBooks: DatabaseReference
    get() = booksRepository.stash(authRepository.userId)
}
