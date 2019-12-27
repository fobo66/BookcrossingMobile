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

package com.bookcrossing.mobile.ui.releasebook

import android.net.Uri

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * View for release new book screen
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface ReleaseAcquiredBookView : MvpView {
  /**
   * User has selected cover for the book
   */
  fun onCoverChosen(coverUri: Uri?)

  /**
   * Show cover after user has typed a name of the book
   */
  fun showCover()

  /**
   * Book was successfully released
   */
  fun onReleased(newKey: String)

  /**
   * Error happened during releasing the book
   */
  fun onFailedToRelease()

  /**
   * Show prompt to user about the need to provide default city
   */
  fun askUserToProvideDefaultCity()
}
