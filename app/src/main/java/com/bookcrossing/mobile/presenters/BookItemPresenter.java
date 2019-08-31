package com.bookcrossing.mobile.presenters;

import androidx.annotation.NonNull;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

@InjectViewState public class BookItemPresenter extends BasePresenter<BookItemView> {

  public static final String TAG = "BookItemPresenter";

  public void releaseCurrentBook(@NonNull String key, @NonNull String position) {
    acquiredBooks().child(key).removeValue();
    books().child(key).child("city").setValue(getCity());
    books().child(key).child("positionName").setValue(position);
    books().child(key).child("free").setValue(true);
  }

  public void unstashCurrentBook(String key) {
    stash().child(key).removeValue();
    firebaseWrapper.getFcm().unsubscribeFromTopic(key);
  }
}
