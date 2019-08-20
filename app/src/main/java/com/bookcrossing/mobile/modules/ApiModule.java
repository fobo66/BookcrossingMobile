package com.bookcrossing.mobile.modules;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import dagger.Module;
import dagger.Provides;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */

@Module public class ApiModule {

  @Provides public FirebaseDatabase provideDatabase() {
    return FirebaseDatabase.getInstance();
  }

  @Provides public FirebaseStorage provideStorage() {
    return FirebaseStorage.getInstance();
  }

  @Provides public FirebaseAuth provideAuth() {
    return FirebaseAuth.getInstance();
  }

  @Provides public FirebaseMessaging provideFCM() {
    return FirebaseMessaging.getInstance();
  }
}
