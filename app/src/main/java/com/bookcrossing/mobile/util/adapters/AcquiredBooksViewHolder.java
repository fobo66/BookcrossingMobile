package com.bookcrossing.mobile.util.adapters;

import android.view.View;
import android.widget.TextView;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by fobo66 on 09.05.17.
 */

public class AcquiredBooksViewHolder extends BaseViewHolder implements BookItemView {

    @BindView(R.id.book_name)
    TextView bookName;

    @BindView(R.id.author)
    TextView author;

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

    }
}