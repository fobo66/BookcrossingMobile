package com.bookcrossing.mobile.presenters;

import android.net.Uri;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.scan.ScanView;
import com.bookcrossing.mobile.util.Constants;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 11.06.17.
 */

@InjectViewState public class ScanPresenter extends BasePresenter<ScanView> {

  public void checkBookcrossingUri(String possibleBookcrossingUri) {
    Uri uri = Uri.parse(possibleBookcrossingUri);
    if (isValidBookcrossingUri(uri)) {
      getViewState().onBookCodeScanned(uri);
    } else {
      getViewState().onIncorrectCodeScanned();
    }
  }

  private boolean isValidBookcrossingUri(Uri uri) {
    return uri.getAuthority().equalsIgnoreCase(Constants.PACKAGE_NAME)
        && uri.getScheme()
        .equalsIgnoreCase("bookcrossing")
        && uri.getPath().equalsIgnoreCase("/book")
        && uri.getQueryParameter(Constants.EXTRA_KEY) != null;
  }
}
