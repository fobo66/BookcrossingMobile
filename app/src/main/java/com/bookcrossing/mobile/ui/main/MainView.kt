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

package com.bookcrossing.mobile.ui.main


import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66></fobo66>@protonmail.com>
 * Created by fobo66 on 21.12.2016.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
  /** Show button to release new book */
  fun showReleaseBookButton()
}
