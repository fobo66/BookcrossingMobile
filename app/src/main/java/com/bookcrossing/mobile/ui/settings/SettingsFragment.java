package com.bookcrossing.mobile.ui.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.PreferenceChangeListener;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 04.06.17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference.OnPreferenceChangeListener listener = new PreferenceChangeListener();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_main, rootKey);
        findPreference(Constants.EXTRA_DEFAULT_CITY).setOnPreferenceChangeListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
