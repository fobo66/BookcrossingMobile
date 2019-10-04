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

package com.bookcrossing.mobile.presenters

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.edit
import com.bookcrossing.mobile.code.BookStickerEncoder
import com.bookcrossing.mobile.code.BookStickerSaver
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Date
import com.bookcrossing.mobile.ui.create.BookCreateView
import com.bookcrossing.mobile.util.EXTRA_CITY
import com.bookcrossing.mobile.util.EXTRA_DEFAULT_CITY
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.StorageMetadata
import com.google.zxing.WriterException
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import moxy.InjectViewState
import java.util.Calendar

@InjectViewState
class BookCreatePresenter : BasePresenter<BookCreateView>() {

  private val book: Book = Book()
  private var tempCoverUri: Uri? = null

  init {
    book.isFree = true
  }

  private fun uploadCover(key: String?) {
    if (tempCoverUri != null && firebaseWrapper.auth.currentUser != null) {
      val metadata = StorageMetadata.Builder()
        .setContentType("image/jpeg")
        .build()
      resolveCover(key).putFile(tempCoverUri!!, metadata)
    }
  }

  fun saveCoverTemporarily(result: FileData) {
    tempCoverUri = Uri.fromFile(result.file)
    viewState.onCoverChosen(tempCoverUri)
  }

  fun onNameChange(name: String) {
    book.name = name
    viewState.showCover()
  }

  fun onAuthorChange(author: String) {
    book.author = author
  }

  fun onPositionChange(position: String) {
    book.positionName = position
  }

  fun onDescriptionChange(description: String) {
    book.description = description
  }

  fun publishBook() {
    book.city = getCity()
    setPublicationDate()
    val newBookReference = books().push()
    newBookReference.setValue(book)
      .addOnSuccessListener {
        val key: String = newBookReference.key.orEmpty()
        if (key.isNotEmpty()) {
          uploadCover(key)
          viewState.onReleased(key)
        }
      }
      .addOnFailureListener { e ->
        Crashlytics.logException(e)
        viewState.onFailedToRelease()
      }
  }

  private fun setPublicationDate() {
    val calendar = Calendar.getInstance()
    val date = Date(
      calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH), calendar.timeInMillis
    )
    book.wentFreeAt = date
  }

  fun generateQrCode(key: String): Bitmap? {
    return try {
      BookStickerEncoder().encodeBookAsQrCode(buildBookUri(key).toString())
    } catch (e: WriterException) {
      Log.e(TAG, "Failed to encode book key to QR code")
      Crashlytics.logException(e)
      null
    } catch (e: IllegalArgumentException) {
      Log.e(TAG, "Failed to save QR code in bitmap")
      Crashlytics.logException(e)
      null
    }
  }

  fun saveSticker(
    sticker: Bitmap,
    stickerName: String,
    stickerDescription: String
  ) {
    BookStickerSaver(systemServicesWrapper.app.contentResolver).saveSticker(
      stickerName, stickerDescription, sticker
    )
  }

  fun resolveUserCity(): Maybe<String> {
    return systemServicesWrapper.locationRepository.getLastKnownUserLocation()
      .flatMapMaybe<String> { location ->
        systemServicesWrapper.locationRepository.resolveUserCity(
          location
        )
      }
      .observeOn(AndroidSchedulers.mainThread())
  }

  fun saveCity(city: String) {
    systemServicesWrapper.preferences.edit {
      putString(EXTRA_CITY, city)
      putString(EXTRA_DEFAULT_CITY, city)
    }
  }

  companion object {
    const val TAG = "BookCreatePresenter"
  }
}
