package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.acquire.BookAcquireView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

@InjectViewState public class BookAcquirePresenter extends BasePresenter<BookAcquireView> {

  public void setKeyExists(boolean keyExists) {
    this.keyExists = keyExists;
  }

  private boolean keyExists;

  public void handleAcquisition(final String key) {
    books().child(key).child("free").setValue(false);

    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
        .map(new Function<Book, Book>() {
          @Override public Book apply(@NonNull Book book) throws Exception {
            acquiredBooks().child(key).setValue(book);
            return book;
          }
        })
        .subscribe(new Consumer<Book>() {
          @Override public void accept(@NonNull Book book) throws Exception {
            getViewState().onAcquired();
          }
        }));
  }

  public boolean isKeyValid(final String key) {
    books().addListenerForSingleValueEvent(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild(key)) {
          setKeyExists(true);
        } else {
          setKeyExists(false);
          getViewState().onIncorrectKey();
        }
      }

      @Override public void onCancelled(DatabaseError databaseError) {

      }
    });

    return keyExists;
  }
}
