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

package com.bookcrossing.mobile.ui.main

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.EXTRA_TARGET_FRAGMENT
import com.bookcrossing.mobile.util.KEY_CONSENT_STATUS
import com.bookcrossing.mobile.util.PRIVACY_POLICY_URL
import com.bookcrossing.mobile.util.listeners.BookListener
import com.firebase.ui.auth.AuthUI
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.android.material.navigation.NavigationView
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

class MainActivity : BaseActivity(), BookListener, OnMenuItemClickListener {

  @BindView(R.id.coord_layout)
  lateinit var coordinatorLayout: CoordinatorLayout

  @BindView(R.id.toolbar)
  lateinit var toolbar: Toolbar

  @BindView(R.id.nav_view)
  lateinit var navigationView: NavigationView

  @BindView(R.id.drawer_layout)
  lateinit var drawer: DrawerLayout

  private val navController: NavController by lazy {
    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    navHostFragment.navController
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)

    coordinatorLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

    setupToolbar()
    checkForConsent()
    resolveNavigationToFragment(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    super.onSaveInstanceState(outState, outPersistentState)
    outState.putBundle("navHostState", findNavController(R.id.nav_host_fragment).saveState())
  }

  private fun setupToolbar() {
    val appBarConfiguration = AppBarConfiguration(navController.graph, drawer)
    toolbar.setupWithNavController(navController, appBarConfiguration)
    navigationView.setupWithNavController(navController)
    toolbar.inflateMenu(R.menu.menu_main)
    toolbar.setOnMenuItemClickListener(this)
  }

  private fun checkForConsent() {
    val consentInformation = ConsentInformation.getInstance(this)

    val publisherIds = arrayOf(resources.getString(R.string.admob_publisher_id))
    consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
      override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
        if (consentInformation.isRequestLocationInEeaOrUnknown) {
          if (consentStatus == ConsentStatus.UNKNOWN) {
            val privacyUrl: URL
            try {
              privacyUrl = URL(PRIVACY_POLICY_URL)
            } catch (e: MalformedURLException) {
              throw IllegalArgumentException("Privacy policy URL was malformed", e)
            }

            val form = ConsentForm.Builder(this@MainActivity, privacyUrl).withListener(
              object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                  Timber.d("onConsentFormLoaded: Consent form loaded successfully.")
                }

                override fun onConsentFormOpened() {
                  Timber.d("onConsentFormOpened: Consent form was displayed.")
                }

                override fun onConsentFormClosed(
                  consentStatus: ConsentStatus?,
                  userPrefersAdFree: Boolean?
                ) {
                  Timber.d("onConsentFormClosed: $consentStatus")
                  saveConsentStatus(consentStatus)
                }

                override fun onConsentFormError(errorDescription: String?) {
                  Timber.d("User's consent status failed to update: $errorDescription")
                }
              }).withPersonalizedAdsOption().withNonPersonalizedAdsOption().build()

            form.load()
            form.show()
          } else {
            saveConsentStatus(consentStatus)
          }
        }
      }

      override fun onFailedToUpdateConsentInfo(errorDescription: String) {
        Timber.d("User's consent status failed to update: $errorDescription")
      }
    })
  }

  private fun saveConsentStatus(consentStatus: ConsentStatus?) {
    PreferenceManager.getDefaultSharedPreferences(applicationContext)
      .edit {
        putString(KEY_CONSENT_STATUS, consentStatus?.toString())
      }
  }

  private fun resolveNavigationToFragment(savedInstanceState: Bundle?) {
    if (intent != null) {
      val destinationFragment = intent.getStringExtra(EXTRA_TARGET_FRAGMENT)
      when {
        destinationFragment != null -> when {
          "BookReleaseFragment".equals(
            destinationFragment,
            ignoreCase = true
          ) -> navController.navigate(R.id.bookReleaseFragment)
          "ProfileFragment".equals(
            destinationFragment,
            ignoreCase = true
          ) -> navController.navigate(R.id.profileFragment)
          "MapsFragment".equals(
            destinationFragment,
            ignoreCase = true
          ) -> navController.navigate(R.id.mapsFragment)
        }
        savedInstanceState != null -> navController.restoreState(
          savedInstanceState.getBundle(
            "navHostState"
          )
        )
      }
    }
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.menu_action_search -> {
        findNavController(R.id.nav_host_fragment).navigate(R.id.searchFragment)
        item.expandActionView()
        true
      }
      R.id.menu_action_logout -> {
        AuthUI.getInstance().signOut(this).addOnCompleteListener { finish() }
        true
      }
      else -> false
    }
  }

  override fun onBookSelected(bookKey: String) {
    val bookActivityArgs = bundleOf(EXTRA_KEY to bookKey)
    val extras = ActivityNavigatorExtras(ActivityOptionsCompat.makeSceneTransitionAnimation(this))
    findNavController(R.id.nav_host_fragment).navigate(
      R.id.bookActivity,
      bookActivityArgs,
      null,
      extras
    )
  }

  override fun onBookReleased(bookKey: String) {
    findNavController(R.id.nav_host_fragment).popBackStack()
    onBookSelected(bookKey)
  }

  override fun onBookAdd() {
    findNavController(R.id.nav_host_fragment).navigate(R.id.bookReleaseFragment)
  }
}