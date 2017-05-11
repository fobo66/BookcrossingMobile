package com.bookcrossing.mobile.util.adapters;

import android.view.View;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

import butterknife.OnClick;

public class AcquiredBooksViewHolder extends BooksViewHolder implements BookItemView {

    public AcquiredBooksViewHolder(View view) {
        super(view);
    }

    @Override
    public void bind(Book book) {
        bookName.setText(book.getName());
        author.setText(book.getAuthor());
    }

    @OnClick(R.id.release_button)
    public void onClick() {
        itemPresenter.releaseCurrentBook(key);
    }
}
