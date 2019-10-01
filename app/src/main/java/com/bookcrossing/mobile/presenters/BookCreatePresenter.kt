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

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.edit
import com.bookcrossing.mobile.code.BookStickerEncoder
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Date
import com.bookcrossing.mobile.ui.create.BookCreateView
import com.bookcrossing.mobile.util.EXTRA_CITY
import com.bookcrossing.mobile.util.EXTRA_DEFAULT_CITY
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.StorageMetadata
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
    } catch (e: Exception) {
      e.printStackTrace()
      Crashlytics.logException(e)
      null
    }

  }

  fun saveSticker(
    sticker: Bitmap,
    stickerName: String,
    stickerDescription: String
  ) {
    val values = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, stickerName)
      put(MediaStore.Images.Media.DESCRIPTION, stickerDescription)
      put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
      put(MediaStore.Images.Media.WIDTH, sticker.width)
      put(MediaStore.Images.Media.HEIGHT, sticker.height)
      put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val resolver = systemServicesWrapper.app.contentResolver
    val collection = MediaStore.Images.Media
      .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val item = resolver.insert(collection, values)

    resolver.openOutputStream(item!!)
      .use { stream ->
        sticker.compress(Bitmap.CompressFormat.JPEG, 95, stream)
      }

    values.clear()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    resolver.update(item, values, null, null)
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
}
