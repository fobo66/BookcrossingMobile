package com.bookcrossing.mobile.modules;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by fobo66 on 15.11.2016.
 */

@Module
public class PrefModule {

    @Provides
    SharedPreferences provideSharedPreferences(App app) {
        return PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
    }
}
