package com.bookcrossing.mobile.presenters;

import android.support.annotation.NonNull;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

@InjectViewState public class BookItemPresenter extends BasePresenter<BookItemView> {

  public static final String TAG = "BookItemPresenter";

  public void releaseCurrentBook(@NonNull String key, @NonNull String position) {
    acquiredBooks().child(key).removeValue();
    books().child(key).child("free").setValue(true);
    books().child(key).child("positionName").setValue(position);
  }
}
