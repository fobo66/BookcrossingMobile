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
import butterknife.BindView
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton.POSITIVE
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.presenters.AcquiredBookItemPresenter
import com.bookcrossing.mobile.ui.profile.AcquiredBookItemView
import com.bookcrossing.mobile.util.ValidationResult.Invalid
import com.bookcrossing.mobile.util.ValidationResult.OK
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
    MaterialDialog(itemView.context).show {
      title(R.string.release_book_dialog_title)
      positiveButton(R.string.release_book, click = { dialog ->
        presenter.releaseCurrentBook(
          key,
          dialog.getInputField().text.toString()
        )
      })
      input(
        hintRes = R.string.hint_position,
        callback = { dialog, input: CharSequence ->
          when (val result = presenter.validateInput(input)) {
            is OK -> {
              dialog.getInputField().error = null
              dialog.getActionButton(POSITIVE).isEnabled = true
            }
            is Invalid -> {
              dialog.getInputField().error =
                itemView.context.getString(result.messageId)
              dialog.getActionButton(POSITIVE).isEnabled = false
            }
          }
        })
    }
  }
}
