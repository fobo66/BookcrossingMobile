/*
 *    Copyright  2019 Andrey Mukamolov
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

package com.bookcrossing.mobile.ui.settings;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.BookListenerDelegate;
import com.bookcrossing.mobile.util.ConstantsKt;
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

    findPreference(ConstantsKt.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(
        preferenceChangeListener);
    findPreference(ConstantsKt.KEY_CONSENT_STATUS).setOnPreferenceChangeListener(
        preferenceChangeListener);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    delegate = new BookListenerDelegate(context);
  }

  @Override public void onDestroy() {
    super.onDestroy();

    findPreference(ConstantsKt.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(null);
    findPreference(ConstantsKt.KEY_CONSENT_STATUS).setOnPreferenceChangeListener(null);
  }

  @Override public void onDetach() {
    super.onDetach();
    delegate.detachListener();
  }

  @Override public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.pref_main, rootKey);
  }
}
