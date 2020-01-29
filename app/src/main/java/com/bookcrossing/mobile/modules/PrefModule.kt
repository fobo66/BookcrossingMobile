package com.bookcrossing.mobile.modules

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides

/**
 * Created by fobo66 on 15.11.2016.
 */
@Module
class PrefModule {
  @Provides
  fun provideSharedPreferences(app: App): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
}