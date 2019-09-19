package com.bookcrossing.mobile.ui.bookpreview;

import com.bookcrossing.mobile.models.Book;

import moxy.MvpView;

/**
 * Created by fobo66 on 22.1.17.
 */

public interface BookItemView extends MvpView {
  void bind(Book book);
}
