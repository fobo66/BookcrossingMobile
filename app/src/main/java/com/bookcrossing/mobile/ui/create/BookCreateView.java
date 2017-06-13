package com.bookcrossing.mobile.ui.create;

import android.net.Uri;
import com.arellomobile.mvp.MvpView;

public interface BookCreateView extends MvpView {
  void OnCoverChosen(Uri coverUri);

  void OnNameChange();

  void OnReleased(String newKey);
}
