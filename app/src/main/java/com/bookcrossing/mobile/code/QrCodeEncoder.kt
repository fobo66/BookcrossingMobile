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

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

/**
 * Encode some string, such as book ID, into the QR code image
 */
class QrCodeEncoder(
  private val format: BarcodeFormat = QR_CODE
) {
  private val hints: EnumMap<EncodeHintType, Any> =
    EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)

  init {
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
  }

  /**
   * Encode info into the QR code image
   *
   * @param contents Info to encode, such as book key
   * @param imgWidth Width of the output image
   * @param imgHeight Height of the output image
   *
   * @return QR code as an image
   */
  @Throws(WriterException::class, IllegalArgumentException::class)
  fun encode(
    contents: String?,
    imgWidth: Int = 450,
    imgHeight: Int = 450
  ): Bitmap? {
    if (contents == null) {
      return null
    }
    val writer = MultiFormatWriter()
    val bitMatrix: BitMatrix

    bitMatrix = writer.encode(contents, format, imgWidth, imgHeight, hints)

    val pixels = processBitMatrix(bitMatrix)

    val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, bitMatrix.width, 0, 0, bitMatrix.width, bitMatrix.height)
    return bitmap
  }

  private fun processBitMatrix(bitMatrix: BitMatrix): IntArray {
    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
      val offset = y * width
      for (x in 0 until width) {
        pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
      }
    }
    return pixels
  }
}
