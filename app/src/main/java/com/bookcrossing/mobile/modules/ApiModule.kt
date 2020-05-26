package com.bookcrossing.mobile.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

/**
 * Module to provide Firebase dependencies via Dagger
 *
 * Created by fobo66 on 15.11.2016.
 */
@Module
class ApiModule {

  /** Firebase Realtime Database */
  @Provides
  fun provideDatabase() = FirebaseDatabase.getInstance()

  /** Firebase Storage */
  @Provides
  fun provideStorage() = FirebaseStorage.getInstance()

  /** Firebase Auth */
  @Provides
  fun provideAuth() = FirebaseAuth.getInstance()

  /** Firebase Cloud Messaging */
  @Provides
  fun provideFCM() = FirebaseMessaging.getInstance()

  /** Firebase MLKit barcode scanner options */
  @Provides
  fun provideMlKitOptions() =
    FirebaseVisionBarcodeDetectorOptions.Builder()
      .setBarcodeFormats(
        FirebaseVisionBarcode.FORMAT_QR_CODE
      )
      .build()

  /** Firebase MLKit barcode scanner */
  @Provides
  fun provideMlKitBarcodeDetector(options: FirebaseVisionBarcodeDetectorOptions) =
    FirebaseVision.getInstance()
      .getVisionBarcodeDetector(options)
}