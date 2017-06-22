package com.bookcrossing.mobile.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import dagger.Lazy;
import javax.inject.Inject;

public class FirebaseWrapper {
  @Inject Lazy<FirebaseStorage> storageLazy;

  @Inject Lazy<FirebaseAuth> authLazy;

  @Inject Lazy<FirebaseDatabase> databaseLazy;

  @Inject Lazy<FirebaseMessaging> fcmLazy;

  public FirebaseMessaging getFcm() {
    return fcmLazy.get();
  }

  public FirebaseStorage getStorage() {
    return storageLazy.get();
  }

  public FirebaseAuth getAuth() {
    return authLazy.get();
  }

  public FirebaseDatabase getDatabase() {
    return databaseLazy.get();
  }
}
