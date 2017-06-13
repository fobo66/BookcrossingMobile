package com.bookcrossing.mobile.util.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

public class AcquiredBooksViewHolder extends BooksViewHolder implements BookItemView {

  public AcquiredBooksViewHolder(View view) {
    super(view);
  }

  @Override public void bind(Book book) {
    bookName.setText(book.getName());
    author.setText(book.getAuthor());
  }

  @OnClick(R.id.release_button) public void release() {
    new MaterialDialog.Builder(itemView.getContext()).title("Specify new book's position")
        .positiveText(R.string.release_book)
        .input(R.string.hint_position, R.string.filler_position, false,
            new MaterialDialog.InputCallback() {
              @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                if (input.length() < 5 || input.length() > 50) {
                  dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                } else {
                  dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                }
              }
            })
        .inputRange(5, 50)
        .alwaysCallInputCallback()
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            itemPresenter.releaseCurrentBook(key, dialog.getInputEditText().getText().toString());
          }
        })
        .show();
  }
}
