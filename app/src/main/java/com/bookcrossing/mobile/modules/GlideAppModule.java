package com.bookcrossing.mobile.modules;

import android.content.Context;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import java.io.InputStream;

/**
 * (c) 2018 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 5/19/18.
 */
@GlideModule public class GlideAppModule extends AppGlideModule {

  @Override public void registerComponents(@NonNull Context context, @NonNull Glide glide,
      @NonNull Registry registry) {
    // Register FirebaseImageLoader to handle StorageReference
    registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
  }
}
