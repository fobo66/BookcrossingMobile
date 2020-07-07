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

package com.bookcrossing.mobile.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
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

  /** MLKit barcode scanner options */
  @Provides
  fun provideMlKitOptions() =
    BarcodeScannerOptions.Builder()
      .setBarcodeFormats(
        Barcode.FORMAT_QR_CODE
      )
      .build()

  /** MLKit barcode scanner */
  @Provides
  fun provideMlKitBarcodeDetector(options: BarcodeScannerOptions) =
    BarcodeScanning.getClient(options)
}