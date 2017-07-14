package com.bookcrossing.mobile.ui.create;

import android.net.Uri;
import com.arellomobile.mvp.MvpView;

public interface BookCreateView extends MvpView {
  void onCoverChosen(Uri coverUri);

  void onNameChange();

  void onReleased(String newKey);

  void onFailedToRelease();
}
