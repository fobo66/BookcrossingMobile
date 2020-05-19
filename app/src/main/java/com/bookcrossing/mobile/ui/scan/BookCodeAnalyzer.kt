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

package com.bookcrossing.mobile.ui.scan

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import timber.log.Timber

class BookCodeAnalyzer : ImageAnalysis.Analyzer {

  private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
    0 -> FirebaseVisionImageMetadata.ROTATION_0
    90 -> FirebaseVisionImageMetadata.ROTATION_90
    180 -> FirebaseVisionImageMetadata.ROTATION_180
    270 -> FirebaseVisionImageMetadata.ROTATION_270
    else -> throw IllegalStateException("Rotation must be 0, 90, 180, or 270.")
  }

  @ExperimentalGetImage
  override fun analyze(image: ImageProxy) {
    val mediaImage = image.image
    val imageRotation = degreesToFirebaseRotation(image.imageInfo.rotationDegrees)
    if (mediaImage != null) {
      val detectableImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
      val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(
          FirebaseVisionBarcode.FORMAT_QR_CODE
        )
        .build()
      val detector = FirebaseVision.getInstance()
        .getVisionBarcodeDetector(options)

      val result = detector.detectInImage(detectableImage)
        .addOnSuccessListener { barcodes ->
          for (barcode in barcodes) {
            val bounds = barcode.boundingBox
            val corners = barcode.cornerPoints

            val rawValue = barcode.rawValue

            val valueType = barcode.valueType
            // See API reference for complete list of supported types
            Timber.d("Scanned barcode: %s", rawValue)
          }
        }
        .addOnFailureListener {
          Timber.e(it, "Failed to scan barcode")
        }
    }
  }
}