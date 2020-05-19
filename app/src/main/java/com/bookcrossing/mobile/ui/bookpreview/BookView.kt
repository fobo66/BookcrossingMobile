/*
 *    Copyright  2019 Andrey Mukamolov
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

package com.bookcrossing.mobile.ui.bookpreview

import com.bookcrossing.mobile.models.Book
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * View for book screen
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface BookView : MvpView {

  /**
   * Book info is ready to be shown
   */
  fun onBookLoaded(book: Book)

  /**
   * Error happened during loading book
   */
  fun onErrorToLoadBook()

  /**
   * Book was added to stash successfully
   */
  fun onBookStashed()

  /**
   * Error happened during adding book to stash
   */
  fun onBookUnstashed()

  /**
   * User's report was sent
   */
  fun onAbuseReported()
}