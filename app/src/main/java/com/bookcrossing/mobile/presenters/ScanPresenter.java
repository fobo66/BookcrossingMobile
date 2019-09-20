/*
 *    Copyright  2019 Andrey Mukamolov
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.presenters;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.bookcrossing.mobile.ui.scan.ScanView;
import com.bookcrossing.mobile.util.ConstantsKt;
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
      return uri.getAuthority().equalsIgnoreCase(ConstantsKt.PACKAGE_NAME)
        && uri.getScheme()
        .equalsIgnoreCase("bookcrossing")
        && uri.getPath().equalsIgnoreCase("/book")
        && uri.getQueryParameter(ConstantsKt.EXTRA_KEY) != null;
  }
}
