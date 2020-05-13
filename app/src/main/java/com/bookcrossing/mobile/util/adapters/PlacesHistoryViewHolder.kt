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
package com.bookcrossing.mobile.util.adapters

import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.ui.map.MapActivity.Companion.getStartIntent

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 10.06.17.
 */
class PlacesHistoryViewHolder(view: View) : BaseViewHolder(view) {
  @BindView(id.placesHistoryItem)
  lateinit var positionName: TextView

  private var coordinates: Coordinates? = null

  fun bind(positionName: String?, coordinates: Coordinates) {
    this.coordinates = coordinates
    this.positionName.text = positionName
  }

  @OnClick(id.card)
  fun goToPlace() {
    itemView.context
      .startActivity(getStartIntent(itemView.context, coordinates))
  }
}