package com.bookcrossing.mobile.ui.bookpreview;

import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.models.Book;

/**
 * Created by fobo66 on 23.3.17.
 */

public interface BookView extends MvpView {
  void onBookLoaded(Book book);

  void onBookStashed();

  void onBookUnstashed();
}
