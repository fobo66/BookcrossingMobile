package com.bookcrossing.mobile.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by fobo66 on 25.4.17.
 */

public class FirebaseWrapper {
    @Inject
    Lazy<FirebaseStorage> storageLazy;

    @Inject
    Lazy<FirebaseAuth> authLazy;

    @Inject
    Lazy<FirebaseDatabase> databaseLazy;

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
