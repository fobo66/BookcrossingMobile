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
import com.bookcrossing.mobile.models.Book
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class BooksAdapter(options: FirebaseRecyclerOptions<Book>) :
  FirebaseRecyclerAdapter<Book, BooksViewHolder>(options) {
  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): BooksViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(layout.book_list_item_main, parent, false)
    return BooksViewHolder(view)
  }

  override fun onBindViewHolder(
    holder: BooksViewHolder,
    position: Int,
    model: Book
  ) {
    holder.setKey(this.getRef(position).key)
    holder.bind(model)
  }
}