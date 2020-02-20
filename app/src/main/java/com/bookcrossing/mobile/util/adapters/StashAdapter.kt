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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.util.BookCoverResolver
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class StashAdapter(
  private val bookCoverResolver: BookCoverResolver,
  options: FirebaseRecyclerOptions<Boolean>
) : FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder>(options) {
  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): StashedBookViewHolder {
    val view =
      LayoutInflater.from(parent.context)
        .inflate(layout.stash_item, parent, false)
    return StashedBookViewHolder(view)
  }

  override fun onBindViewHolder(
    holder: StashedBookViewHolder, position: Int,
    model: Boolean
  ) {
    val key = getRef(position).key.orEmpty()
    holder.key = key
    holder.loadCover(bookCoverResolver.resolveCover(key))
  }
}
