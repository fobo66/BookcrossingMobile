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
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.bookcrossing.mobile.R.drawable
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.ui.bookpreview.BookItemView
import com.bookcrossing.mobile.ui.stash.BookCoverView
import com.bookcrossing.mobile.util.listeners.BookListener
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.StorageReference

/** Viewholder for the books list item */
class BooksViewHolder(view: View) : BaseViewHolder(view), BookItemView, BookCoverView {

  @BindView(id.cover)
  lateinit var cover: ImageView

  @BindView(id.book_name)
  lateinit var bookName: TextView

  @BindView(id.author)
  lateinit var author: TextView

  @BindView(id.current_place)
  lateinit var bookPlace: TextView

  var key: String = ""

  override fun bind(book: Book) {
    bookName.text = book.name
    bookPlace.text = book.positionName
    author.text = book.author
  }

  override fun loadCover(coverReference: StorageReference) {
    GlideApp.with(itemView.context)
      .load(coverReference)
      .placeholder(drawable.ic_book_cover_placeholder)
      .transition(DrawableTransitionOptions.withCrossFade())
      .thumbnail(0.6f)
      .into(cover)
  }

  /** Show detailed screen for the selected book */
  @OnClick(id.card)
  fun selectBook() {
    (itemView.context as BookListener).onBookSelected(key)
  }
}
