package com.bookcrossing.mobile.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.BookListenerDelegate;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.PreferenceChangeListener;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 04.06.17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

  private Preference.OnPreferenceChangeListener preferenceChangeListener =
      new PreferenceChangeListener();
  private BookListenerDelegate delegate;

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    delegate = new BookListenerDelegate(context);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    delegate.setTitle(R.string.action_settings);
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onDetach() {
    super.onDetach();
    delegate.detachListener();
  }

  @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.pref_main, rootKey);
    findPreference(Constants.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(
        preferenceChangeListener);
  }
}
