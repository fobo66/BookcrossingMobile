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
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import javax.inject.Inject


/** CameraX analyzer for QR codes */
class BookCodeAnalyzer @Inject constructor(
  private val detector: BarcodeScanner
) : ImageAnalysis.Analyzer {

  private val barcodesProcessor = PublishProcessor.create<Barcode>()

  @ExperimentalGetImage
  override fun analyze(image: ImageProxy) {
    val mediaImage = image.image
    if (mediaImage != null) {
      val detectableImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

      detector.process(detectableImage)
        .addOnSuccessListener { barcodes ->
          barcodes.forEach { barcode ->
            Timber.d("Scanned barcode: %s", barcode.rawValue)
            barcodesProcessor.offer(barcode)
          }
          image.close()
        }
        .addOnFailureListener {
          Timber.e(it, "Failed to scan barcode")
          image.close()
        }
    } else
      image.close()
  }

  /** Exposes scanned barcodes via Rx flowable stream */
  fun onBarcodeScanned(): Flowable<Barcode> = barcodesProcessor.hide()
}