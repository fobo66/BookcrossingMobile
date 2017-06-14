package com.bookcrossing.mobile.ui.bookpreview;

import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.models.Book;

public interface BookView extends MvpView {
  void onBookLoaded(Book book);

  void onErrorToLoadBook();

  void onBookStashed();

  void onBookUnstashed();
}
