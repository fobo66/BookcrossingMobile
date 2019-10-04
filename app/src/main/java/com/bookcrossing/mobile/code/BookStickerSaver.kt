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

package com.bookcrossing.mobile.code

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

class BookStickerSaver(
  private val resolver: ContentResolver
) {
  fun saveSticker(
    stickerName: String,
    stickerDescription: String,
    sticker: Bitmap
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

    val collection = MediaStore.Images.Media
      .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val itemUri = resolver.insert(collection, values)

    if (itemUri != null) {
      compressSticker(sticker, itemUri)

      values.clear()
      values.put(MediaStore.Images.Media.IS_PENDING, 0)
      resolver.update(itemUri, values, null, null)
    }
  }

  private fun compressSticker(
    sticker: Bitmap,
    uri: Uri,
    compressionQuality: Int = 95
  ) {
    resolver.openOutputStream(uri)
      .use { stream ->
        sticker.compress(Bitmap.CompressFormat.JPEG, compressionQuality, stream)
      }
  }
}
