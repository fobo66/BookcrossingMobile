package com.bookcrossing.mobile.presenters;

import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookView;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import moxy.InjectViewState;

@InjectViewState public class BookPresenter extends BasePresenter<BookView> {

  private boolean stashed = false;

  public void subscribeToBookReference(String key) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
        .subscribe(book -> getViewState().onBookLoaded(book), throwable -> {
          Crashlytics.logException(throwable);
          getViewState().onErrorToLoadBook();
        }));
  }

  public void checkStashingState(String key) {
    unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(stash().child(key))
        .filter(DataSnapshot::exists)
        .subscribe(data -> {
          stashed = (boolean) data.getValue();
          updateStashButtonState();
        }));
  }

  public void handleBookStashing(String key) {
    stashed = !stashed;
    if (stashed) {
      stash().child(key).setValue(stashed);
      firebaseWrapper.getFcm().subscribeToTopic(key);
    } else {
      stash().child(key).removeValue();
      firebaseWrapper.getFcm().unsubscribeFromTopic(key);
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

  public void reportAbuse(String key) {
    Crashlytics.log(String.format("Users complaining to book %s. Consider to check it", key));
    getViewState().onAbuseReported();
  }
}
