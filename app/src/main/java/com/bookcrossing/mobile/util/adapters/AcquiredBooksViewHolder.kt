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

package com.bookcrossing.mobile.util.adapters

import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import butterknife.BindView
import butterknife.OnClick
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.presenters.AcquiredBookItemPresenter
import com.bookcrossing.mobile.ui.profile.AcquiredBookItemView
import com.bookcrossing.mobile.util.EXTRA_KEY
import moxy.presenter.InjectPresenter

/**
 * View holder for acquired books list item
 */
class AcquiredBooksViewHolder(view: View) : MvpBaseViewHolder(view), AcquiredBookItemView {

  @InjectPresenter(tag = AcquiredBookItemPresenter.TAG)
  lateinit var presenter: AcquiredBookItemPresenter

  private lateinit var key: String
  @BindView(R.id.book_name)
  lateinit var bookName: TextView

  @BindView(R.id.author)
  lateinit var author: TextView

  override fun bind(book: Book, key: String?) {
    bookName.text = book.name
    author.text = book.author
    this.key = key.orEmpty()
  }

  @OnClick(R.id.release_button)
  fun release() {
    itemView.findNavController()
      .navigate(R.id.releaseAcquiredBookFragment, bundleOf(EXTRA_KEY to key))
  }
}
