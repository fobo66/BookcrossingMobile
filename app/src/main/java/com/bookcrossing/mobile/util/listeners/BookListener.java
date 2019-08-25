package com.bookcrossing.mobile.util.listeners;

import androidx.annotation.StringRes;

public interface BookListener {
  void onBookSelected(String bookKey);

  void onBookReleased(String bookKey);

  void onBookAdd();

  void setTitle(@StringRes int fragmentTitleId);
}