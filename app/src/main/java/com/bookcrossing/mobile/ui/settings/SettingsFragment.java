package com.bookcrossing.mobile.ui.settings;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.BookListenerDelegate;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.PreferenceChangeListener;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 04.06.17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

  private static final String EMPTY_VALUE = "";
  private Preference.OnPreferenceChangeListener preferenceChangeListener =
      new PreferenceChangeListener();
  private BookListenerDelegate delegate;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    findPreference(Constants.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(
        preferenceChangeListener);
    findPreference(Constants.KEY_CONSENT_STATUS).setOnPreferenceChangeListener(
        preferenceChangeListener);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    delegate = new BookListenerDelegate(context);
  }

  @Override public void onDestroy() {
    super.onDestroy();

    findPreference(Constants.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(null);
    findPreference(Constants.KEY_CONSENT_STATUS).setOnPreferenceChangeListener(null);
  }

  @Override public void onDetach() {
    super.onDetach();
    delegate.detachListener();
  }

  @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.pref_main, rootKey);
  }
}
