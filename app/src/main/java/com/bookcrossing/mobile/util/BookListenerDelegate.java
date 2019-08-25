package com.bookcrossing.mobile.util;

import android.content.Context;
import androidx.annotation.StringRes;
import com.bookcrossing.mobile.util.listeners.BookListener;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 7/2/17.
 */

public class BookListenerDelegate implements BookListener {

  private BookListener listener;

  public BookListenerDelegate(Context context) {
    if (context instanceof BookListener) {
      listener = (BookListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement BookListener");
    }
  }

  public void detachListener() {
    listener = null;
  }

  @Override public void onBookSelected(String bookKey) {
    listener.onBookSelected(bookKey);
  }

  @Override public void onBookReleased(String bookKey) {
    listener.onBookReleased(bookKey);
  }

  @Override public void onBookAdd() {
    listener.onBookAdd();
  }

  @Override public void setTitle(@StringRes int fragmentTitleId) {
    listener.setTitle(fragmentTitleId);
  }
}
