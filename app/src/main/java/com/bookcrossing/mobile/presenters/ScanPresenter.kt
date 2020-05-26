/*
 *    Copyright 2020 Andrey Mukamolov
 *
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
package com.bookcrossing.mobile.presenters

import android.net.Uri
import com.bookcrossing.mobile.ui.scan.BookCodeAnalyzer
import com.bookcrossing.mobile.ui.scan.ScanView
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.PACKAGE_NAME
import io.reactivex.Flowable
import moxy.InjectViewState
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Inject

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 11.06.17.
 */
@InjectViewState
class ScanPresenter @Inject constructor(
  val bookCodeAnalyzer: BookCodeAnalyzer
) : BasePresenter<ScanView>() {

  /**
   * Check validity of the scanned QR code of the book
   */
  fun checkBookcrossingUri(possibleBookcrossingUri: String) {
    val uri = Uri.parse(possibleBookcrossingUri)
    if (uri != null && isValidBookcrossingUri(uri)) {
      viewState.onBookCodeScanned(uri)
    } else {
      viewState.onIncorrectCodeScanned()
    }
  }

  private fun isValidBookcrossingUri(uri: Uri): Boolean {
    return (uri.authority.equals(PACKAGE_NAME, ignoreCase = true) &&
      uri.scheme
        .equals("bookcrossing", ignoreCase = true) &&
      uri.path.equals("/book", ignoreCase = true) &&
      uri.getQueryParameter(EXTRA_KEY) != null)
  }

  /**
   * Process scanned codes
   */
  fun onBarcodeScanned(): Flowable<String> =
    bookCodeAnalyzer.onBarcodeScanned()
      .throttleFirst(1, SECONDS)
      .filter { it.displayValue != null }
      .map { it.displayValue!! }
}