package com.bookcrossing.mobile.presenters;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.bookcrossing.mobile.ui.scan.ScanView;
import com.bookcrossing.mobile.util.Constants;

import moxy.InjectViewState;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 11.06.17.
 */

@InjectViewState public class ScanPresenter extends BasePresenter<ScanView> {

  public void checkBookcrossingUri(String possibleBookcrossingUri) {
    Uri uri = Uri.parse(possibleBookcrossingUri);
      if (uri != null && isValidBookcrossingUri(uri)) {
      getViewState().onBookCodeScanned(uri);
    } else {
      getViewState().onIncorrectCodeScanned();
    }
  }

    private boolean isValidBookcrossingUri(@NonNull Uri uri) {
    return uri.getAuthority().equalsIgnoreCase(Constants.PACKAGE_NAME)
        && uri.getScheme()
        .equalsIgnoreCase("bookcrossing")
        && uri.getPath().equalsIgnoreCase("/book")
        && uri.getQueryParameter(Constants.EXTRA_KEY) != null;
  }
}
