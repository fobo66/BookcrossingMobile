package com.bookcrossing.mobile.ui.bookpreview;

import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.models.Book;

/**
 * Created by fobo66 on 22.1.17.
 */

public interface BookItemView extends MvpView {
    void bind(Book book);
}
