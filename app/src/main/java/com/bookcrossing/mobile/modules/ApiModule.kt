package com.bookcrossing.mobile.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
@Module
class ApiModule {
  @Provides
  fun provideDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

  @Provides
  fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()

  @Provides
  fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

  @Provides
  fun provideFCM(): FirebaseMessaging = FirebaseMessaging.getInstance()
}