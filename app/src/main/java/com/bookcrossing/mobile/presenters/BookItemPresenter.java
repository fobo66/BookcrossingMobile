package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

@InjectViewState
public class BookItemPresenter extends BasePresenter<BookItemView> {

    public static final String TAG = "BookItemPresenter";

    private Book book;

    public void setBook(Book book) {
        this.book = book;
    }

}
