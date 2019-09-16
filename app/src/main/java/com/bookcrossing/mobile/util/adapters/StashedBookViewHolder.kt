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
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.presenters.StashedBookItemPresenter
import com.bookcrossing.mobile.ui.stash.BookCoverView
import com.bookcrossing.mobile.util.listeners.BookListener
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade

class StashedBookViewHolder(view: View) : MvpBaseViewHolder(view), BookCoverView {

  @InjectPresenter(tag = StashedBookItemPresenter.TAG)
  lateinit var presenter: StashedBookItemPresenter

  private lateinit var key: String

  @BindView(R.id.cover)
  lateinit var cover: ImageView

  override fun loadCover() {
    GlideApp.with(itemView.context)
      .load(presenter.resolveCover(key))
      .placeholder(R.drawable.ic_book_cover_placeholder)
      .transition(withCrossFade())
      .thumbnail(0.6f)
      .into(cover)
  }

  @OnClick(R.id.cover)
  fun onCoverClick() {
    (itemView.context as BookListener).onBookSelected(key)
  }

  @OnLongClick(R.id.cover)
  fun onLongClick(): Boolean {
    MaterialDialog(itemView.context).show {
      title(R.string.unstash_book_confirmation_title)
      positiveButton {
        presenter.unstashCurrentBook(key)
      }
    }
    return true
  }

  fun setKey(key: String) {
    this.key = key
  }
}
