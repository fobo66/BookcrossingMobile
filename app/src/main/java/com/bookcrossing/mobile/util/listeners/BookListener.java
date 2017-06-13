package com.bookcrossing.mobile.util.listeners;

/**
 * Created by fobo66 on 21.1.17.
 */

public interface BookListener {
  void onBookSelected(String bookKey);

  void onBookReleased(String bookKey);

  void onBookAdd();
}