package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookView;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

@InjectViewState public class BookPresenter extends BasePresenter<BookView> {

  private boolean stashed = false;

  public void subscribeToBookReference(String key) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
        .subscribe(new Consumer<Book>() {
          @Override public void accept(@NonNull Book book) throws Exception {
            getViewState().onBookLoaded(book);
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(@NonNull Throwable throwable) {
            FirebaseCrash.report(throwable);
            getViewState().onErrorToLoadBook();
          }
        }));
  }

  public void checkStashingState(String key) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(stash().child(key))
        .filter(new Predicate<DataSnapshot>() {
          @Override public boolean test(@NonNull DataSnapshot dataSnapshot) throws Exception {
            return dataSnapshot.exists();
          }
        })
        .subscribe(new Consumer<DataSnapshot>() {
          @Override public void accept(@NonNull DataSnapshot data) throws Exception {
            stashed = (boolean) data.getValue();
            updateStashButtonState();
          }
        }));
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
