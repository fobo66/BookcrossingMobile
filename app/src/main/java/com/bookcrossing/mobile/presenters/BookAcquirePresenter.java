package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.acquire.BookAcquireView;
import durdinapps.rxfirebase2.RxFirebaseDatabase;

@InjectViewState public class BookAcquirePresenter extends BasePresenter<BookAcquireView> {

  public void setKeyExists(boolean keyExists) {
    this.keyExists = keyExists;
  }

  private boolean keyExists;

  public void handleAcquisition(final String key) {
    books().child(key).child("free").setValue(false);

    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
        .map(book -> {
          acquiredBooks().child(key).setValue(book);
          return book;
        })
        .subscribe(book -> getViewState().onAcquired()));
  }

  public boolean isKeyValid(final String key) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books())
        .subscribe(dataSnapshot -> {
          if (dataSnapshot.hasChild(key)) {
            setKeyExists(true);
          } else {
            setKeyExists(false);
            getViewState().onIncorrectKey();
      }
    }));

    return keyExists;
  }
}
