/*
 *    Copyright 2016 Andrey Mukamolov
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

package com.bookcrossing.mobile.presenters

import android.content.SharedPreferences
import android.os.Bundle
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.ui.main.MainView
import com.bookcrossing.mobile.util.Constants
import com.google.ads.consent.ConsentStatus
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.firebase.database.DatabaseReference
import moxy.InjectViewState
import javax.inject.Inject

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 21.12.2016.
 */

@InjectViewState
class MainPresenter : BasePresenter<MainView>() {

  @Inject
  lateinit var preferences: SharedPreferences

  val books: DatabaseReference
    get() = books()

  init {
    App.getComponent().inject(this)
  }

  fun checkForConsent(adBuilder: AdRequest.Builder) {
    val consentStatus = loadConsentStatus()

    if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
      val extras = Bundle()
      extras.putString("npa", "1")

      adBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
    }
  }

  private fun loadConsentStatus(): ConsentStatus {
    return ConsentStatus.valueOf(preferences.getString(Constants.KEY_CONSENT_STATUS, "UNKNOWN")!!)
  }
}
