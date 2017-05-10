package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

@InjectViewState
public class BookItemPresenter extends BasePresenter<BookItemView> {

    public static final String TAG = "BookItemPresenter";


    public void releaseCurrentBook(String key) {
        acquiredBooks().child(key).removeValue();
        books().child(key).child("free").setValue(true);
    }
}
