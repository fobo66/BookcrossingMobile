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

import com.bookcrossing.mobile.interactor.BookInteractor
import com.bookcrossing.mobile.models.BookCode
import com.bookcrossing.mobile.ui.acquire.BookAcquireView
import io.reactivex.Completable
import io.reactivex.Single
import moxy.InjectViewState
import javax.inject.Inject

/** Presenter for acquire book screen */
@InjectViewState
class BookAcquirePresenter @Inject constructor(
  private val bookInteractor: BookInteractor
) : BasePresenter<BookAcquireView>() {

  /** Perform necessary actions for acquisition result, successful or not */
  fun handleAcquisitionResult(code: BookCode) {
    when (code) {
      is BookCode.CorrectCode -> viewState.onAcquired()
      is BookCode.IncorrectCode -> viewState.onIncorrectKey()
    }
  }

  /** Perform necessary actions for acquiring book */
  fun processBookAcquisition(key: String): Completable = bookInteractor.acquireBook(key)

  /** Check if the given key is valid */
  fun validateCode(key: String): Single<BookCode> = bookInteractor.checkBook(key)
}
