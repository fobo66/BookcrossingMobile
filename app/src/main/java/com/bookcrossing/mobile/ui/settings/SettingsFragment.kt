/*
 *    Copyright 2019 Andrey Mukamolov
 *
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
package com.bookcrossing.mobile.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.bookcrossing.mobile.R.xml
import com.bookcrossing.mobile.util.BookListenerDelegate

/**
 * Screen for application preferences
 */
class SettingsFragment : PreferenceFragmentCompat() {
  private lateinit var delegate: BookListenerDelegate
  override fun onAttach(context: Context) {
    super.onAttach(context)
    delegate = BookListenerDelegate(context)
  }

  override fun onDetach() {
    super.onDetach()
    delegate.detachListener()
  }

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(xml.pref_main, rootKey)
  }
}