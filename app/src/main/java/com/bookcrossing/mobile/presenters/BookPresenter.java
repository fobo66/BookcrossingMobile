package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

@InjectViewState public class BookPresenter extends BasePresenter<BookView> {

  private boolean stashed = false;

  public void subscribeToBookReference(String key) {
    Subscription bookSubscription =
        RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
            .subscribe(new Action1<Book>() {
              @Override public void call(Book book) {
                getViewState().onBookLoaded(book);
              }
            });
    unsubscribeOnDestroy(bookSubscription);
  }

  public void checkStashingState(String key) {
    Subscription stashSubscription = RxFirebaseDatabase.observeSingleValueEvent(stash().child(key))
        .filter(new Func1<DataSnapshot, Boolean>() {
          @Override public Boolean call(DataSnapshot dataSnapshot) {
            return dataSnapshot.exists();
          }
        })
        .subscribe(new Action1<DataSnapshot>() {
          @Override public void call(DataSnapshot data) {
            stashed = (boolean) data.getValue();
            updateStashButtonState();
          }
        });
    unsubscribeOnDestroy(stashSubscription);
  }

  public void handleBookStashing(String key) {
    stashed = !stashed;
    if (stashed) {
      stash().child(key).setValue(stashed);
    } else {
      stash().child(key).removeValue();
    }
    updateStashButtonState();
  }

  private void updateStashButtonState() {
    if (stashed) {
      getViewState().onBookStashed();
    } else {
      getViewState().onBookUnstashed();
    }
  }

  public DatabaseReference getPlacesHistory(String key) {
    return placesHistory(key);
  }
}
