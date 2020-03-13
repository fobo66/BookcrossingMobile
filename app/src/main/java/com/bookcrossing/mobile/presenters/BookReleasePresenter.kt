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
import androidx.annotation.IdRes
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.code.QrCodeEncoder
import com.bookcrossing.mobile.data.AuthRepository
import com.bookcrossing.mobile.data.LocationRepository
import com.bookcrossing.mobile.interactor.BookInteractor
import com.bookcrossing.mobile.models.BookBuilder
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.models.Date
import com.bookcrossing.mobile.ui.releasebook.BookReleaseView
import com.bookcrossing.mobile.util.BookCoverResolver
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.InputValidator
import com.bookcrossing.mobile.util.LengthRule
import com.bookcrossing.mobile.util.NotEmptyRule
import com.bookcrossing.mobile.util.PACKAGE_NAME
import com.bookcrossing.mobile.util.ValidationResult
import com.google.firebase.storage.StorageMetadata
import com.google.zxing.WriterException
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import moxy.InjectViewState
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

/**
 * Presenter for book release view
 */
@InjectViewState
class BookReleasePresenter @Inject constructor(
  private val bookInteractor: BookInteractor,
  private val authRepository: AuthRepository,
  private val locationRepository: LocationRepository,
  private val bookCoverResolver: BookCoverResolver
) : BasePresenter<BookReleaseView>() {

  private val isLocationPicked = BehaviorSubject.createDefault(false)

  private val book: BookBuilder = BookBuilder()
  private lateinit var tempCoverUri: Uri
  private val validator =
    InputValidator(NotEmptyRule(), LengthRule(maxLength = 100))

  private fun uploadCover(key: String): Observable<String> {
    val metadata = StorageMetadata.Builder()
      .setContentType("image/jpeg")
      .build()
    return authRepository.onAuthenticated()
      .filter { auth -> auth.currentUser != null }
      .switchMapSingle {
        RxFirebaseStorage.putFile(bookCoverResolver.resolveCover(key), tempCoverUri, metadata)
          .map { key }
      }
      .onErrorReturn { key }
  }

  /**
   * Indicate that location was picked by the user, so we can proceed with release
   */
  fun onLocationPicked(): Observable<Boolean> = isLocationPicked.hide()

  /** Save chosen cover image URI and display it on UI */
  fun saveCoverTemporarily(coverUri: Uri?) {
    if (coverUri != null) {
      tempCoverUri = coverUri
    }
    viewState.onCoverChosen(tempCoverUri)
  }

  /** Compress cover image to save space on Cloud Storage */
  fun compressCoverPhoto(contentResolver: ContentResolver) {
    var coverPhoto: Bitmap? = null
    contentResolver.openInputStream(tempCoverUri)
      .use {
        coverPhoto = BitmapFactory.decodeStream(it)
      }
    contentResolver.openOutputStream(tempCoverUri)
      .use {
        coverPhoto?.compress(
          Bitmap.CompressFormat.JPEG,
          COMPRESSION_QUALITY,
          it
        )
      }
    viewState.onCoverChosen(tempCoverUri)
  }

  /** Create temp file to store cover photo taken by user */
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

  /** Validate user input */
  fun validateInput(input: String): ValidationResult = validator.validate(input)

  /** Save picked location of the book */
  fun locationPicked(coordinates: Coordinates) {
    book.setPosition(coordinates)
    isLocationPicked.onNext(true)
  }

  /** Release book */
  fun releaseBook(): Observable<String> {
    setPublicationDate()
    val newBook = book.createBook()

    return bookInteractor.releaseBook(newBook)
      .flatMapObservable { uploadCover(it) }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { newKey ->
        book.clear()
        viewState.onReleased(newKey)
      }
      .doOnError {
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

  /** Generate QR code for the newly released book*/
  fun generateQrCode(key: String): Bitmap? {
    return try {
      QrCodeEncoder().encode(buildBookUri(key).toString())
    } catch (e: WriterException) {
      Timber.e(e, "Failed to encode book key to QR code")
      null
    } catch (e: IllegalArgumentException) {
      Timber.e(e, "Failed to save QR code in bitmap")
      null
    }
  }

  /**
   * Determine the city of the location of the book
   */
  fun resolveCity(coordinates: Coordinates): Single<String> {
    return locationRepository.resolveCity(
        coordinates.lat,
        coordinates.lng
      )
      .doOnError(Timber::e)
  }

  /**
   * Save city to preferences
   */
  fun saveCity(city: String) {
    book.setCity(city)
  }

  /**
   * Set correct value from user input
   */
  fun handleInputField(@IdRes id: Int, input: String) {
    when (id) {
      R.id.input_name -> book.setName(input)
      R.id.input_author -> book.setAuthor(input)
      R.id.input_position -> book.setPositionName(input)
      R.id.input_description -> book.setDescription(input)
    }
  }

  private fun buildBookUri(key: String): Uri {
    return Uri.Builder()
      .scheme("bookcrossing")
      .authority(PACKAGE_NAME)
      .path("book")
      .appendQueryParameter(EXTRA_KEY, key)
      .build()
  }

  companion object {
    const val COMPRESSION_QUALITY: Int = 60
  }
}
