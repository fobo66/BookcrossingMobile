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
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

/**
 * Utility class for saving generated images, such as stickers, to the Pictures directory, with compression
 */
class BookStickerSaver(
  private val resolver: ContentResolver
) {

  /**
   * Save image to the Pictures directory, with compression
   *
   * @param name Image name
   * @param description Image description saved to metadata
   * @param sticker Generated image of the sticker
   */
  fun saveSticker(
    name: String,
    description: String,
    sticker: Bitmap
  ) {
    val values = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, name)
      put(MediaStore.Images.Media.DESCRIPTION, description)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
      }

      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
      put(MediaStore.Images.Media.WIDTH, sticker.width)
      put(MediaStore.Images.Media.HEIGHT, sticker.height)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Images.Media.IS_PENDING, 1)
      }
    }

    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val itemUri = resolver.insert(collection, values)

    if (itemUri != null) {
      compressSticker(sticker, itemUri)
      resolver.update(itemUri, values, null, null)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(itemUri, values, null, null)
      }
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
