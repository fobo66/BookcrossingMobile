package com.bookcrossing.mobile.util;

import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 03.06.17.
 */

public class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {
  @Override public boolean onPreferenceChange(Preference preference, Object value) {
    String stringValue = value.toString();

    if (preference instanceof ListPreference) {
      ListPreference listPreference = (ListPreference) preference;
      int index = listPreference.findIndexOfValue(stringValue);

      preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
    } else {
      preference.setSummary(stringValue);
    }
    return true;
  }
}
