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

package com.bookcrossing.mobile.ui.map

import com.bookcrossing.mobile.models.Coordinates
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * View for map screen
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface MvpMapView : MvpView {

  /**
   * Book position was loaded, so we can show it on the map as a marker
   */
  fun onBookMarkerLoaded(key: String, coordinates: Coordinates)

  /**
   * Error happened during loading book position
   */
  fun onErrorToLoadMarker()
}