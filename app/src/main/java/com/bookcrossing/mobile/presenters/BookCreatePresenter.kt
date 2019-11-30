/*
 *    Copyright 2019 Andrey Mukamolov
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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.edit
import com.bookcrossing.mobile.code.BookStickerSaver
import com.bookcrossing.mobile.code.QrCodeEncoder
import com.bookcrossing.mobile.models.BookBuilder
import com.bookcrossing.mobile.models.Date
import com.bookcrossing.mobile.ui.create.BookCreateView
import com.bookcrossing.mobile.util.EXTRA_CITY
import com.bookcrossing.mobile.util.EXTRA_DEFAULT_CITY
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.StorageMetadata
import com.google.zxing.WriterException
import durdinapps.rxfirebase2.RxFirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.UUID

/**
 * Presenter for book create view
 */
@InjectViewState
class BookCreatePresenter : BasePresenter<BookCreateView>() {

  private val book: BookBuilder = BookBuilder()
  private lateinit var tempCoverUri: Uri

  private fun uploadCover(key: String): Observable<String>? {
    val metadata = StorageMetadata.Builder()
      .setContentType("image/jpeg")
      .build()
    return RxFirebaseAuth.observeAuthState(firebaseWrapper.auth)
      .filter { auth -> auth.currentUser != null }
      .switchMapSingle {
        RxFirebaseStorage.putFile(resolveCover(key), tempCoverUri, metadata)
          .map { key }
      }
      .onErrorReturn { key }
  }

  fun saveCoverTemporarily(coverUri: Uri?) {
    if (coverUri != null) {
      tempCoverUri = coverUri
    }
    viewState.onCoverChosen(tempCoverUri)
  }

  fun compressCoverPhoto(contentResolver: ContentResolver) {
    var coverPhoto: Bitmap? = null
    contentResolver.openInputStream(tempCoverUri)
      .use {
        coverPhoto = BitmapFactory.decodeStream(it)
      }
    contentResolver.openOutputStream(tempCoverUri)
      .use {
        coverPhoto?.compress(Bitmap.CompressFormat.JPEG, 60, it)
      }
    viewState.onCoverChosen(tempCoverUri)
  }

  @Throws(IOException::class)
  fun createImageFile(storageDir: File?): File {
    return File.createTempFile(
      "cover_${UUID.randomUUID()}_",
      ".jpg",
      storageDir
    ).also {
      tempCoverUri = Uri.fromFile(it)
    }
  }

  fun onNameChange(name: String) {
    book.setName(name)
    viewState.showCover()
  }

  fun onAuthorChange(author: String) {
    book.setAuthor(author)
  }

  fun onPositionChange(position: String) {
    book.setPositionName(position)
  }

  fun onDescriptionChange(description: String) {
    book.setDescription(description)
  }

  fun publishBook(city: String): Observable<String> {
    setPublicationDate()
    val newBook = book.createBook()
    newBook.city = city
    val newBookReference = books().push()
    val key = newBookReference.key.orEmpty()

    return RxFirebaseDatabase.setValue(newBookReference, newBook)
      .andThen(uploadCover(key))
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        viewState.onReleased(it)
      }
      .doOnError {
        Crashlytics.logException(it)
        Timber.e(it, "Failed to release book")
        viewState.onFailedToRelease()
      }
  }

  private fun setPublicationDate() {
    val calendar = Calendar.getInstance()
    val date = Date(
      calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH), calendar.timeInMillis
    )
    book.setWentFreeAt(date)
  }

  fun generateQrCode(key: String): Bitmap? {
    return try {
      QrCodeEncoder().encode(buildBookUri(key).toString())
    } catch (e: WriterException) {
      Timber.e("Failed to encode book key to QR code")
      Crashlytics.logException(e)
      null
    } catch (e: IllegalArgumentException) {
      Timber.e("Failed to save QR code in bitmap")
      Crashlytics.logException(e)
      null
    }
  }

  /**
   * Save generated sticker to device's default location for pictures
   *
   * @param stickerName Image name
   * @param stickerDescription Image description saved to metadata
   * @param sticker Generated image of the sticker
   */
  fun saveSticker(
    sticker: Bitmap,
    stickerName: String,
    stickerDescription: String
  ) {
    BookStickerSaver(systemServicesWrapper.app.contentResolver).saveSticker(
      stickerName, stickerDescription, sticker
    )
  }

  /**
   * Determine the city in which the user currently resides to set location of the released book
   */
  fun resolveUserCity(): Maybe<String> {
    return systemServicesWrapper.locationRepository.getLastKnownUserLocation()
      .flatMapMaybe<String> { location ->
        systemServicesWrapper.locationRepository.resolveUserCity(
          location
        )
      }
      .doOnSuccess { city -> saveCity(city) }
      .doOnError {
        Timber.e(it, "Failed to resolve city")
        viewState.askUserToProvideDefaultCity()
      }
  }

  /**
   * Save city to preferences
   */
  fun saveCity(city: String) {
    systemServicesWrapper.preferences.edit {
      putString(EXTRA_CITY, city)
      putString(EXTRA_DEFAULT_CITY, city)
    }
  }
}
