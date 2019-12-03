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

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
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
import com.bookcrossing.mobile.util.RC_SIGN_IN
import com.bookcrossing.mobile.util.listeners.BookListener
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)

    coordinatorLayout.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

    setupToolbar()

    checkForConsent()

    val auth = FirebaseAuth.getInstance()
    if (auth.currentUser != null) {
      resolveNavigationToFragment(savedInstanceState)
    } else {
      startActivityForResult(
        AuthUI.getInstance()
          .createSignInIntentBuilder()
          .setAvailableProviders(
            listOf(AuthUI.IdpConfig.GoogleBuilder().build())
          )
          .build(), RC_SIGN_IN
      )
    }
  }

  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    super.onSaveInstanceState(outState, outPersistentState)
    outState.putBundle("navHostState", findNavController(R.id.nav_host_fragment).saveState())
  }

  private fun setupToolbar() {
    val navController = findNavController(R.id.nav_host_fragment)
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
              throw RuntimeException(e)
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
    val navController = findNavController(R.id.nav_host_fragment)
    if (intent != null) {
      val whereToGo = intent.getStringExtra(EXTRA_TARGET_FRAGMENT)
      when {
        whereToGo != null -> when {
          "BookReleaseFragment".equals(
            whereToGo,
            ignoreCase = true
          ) -> navController.navigate(R.id.bookReleaseFragment)
          "ProfileFragment".equals(
            whereToGo,
            ignoreCase = true
          ) -> navController.navigate(R.id.profileFragment)
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
    when (item?.itemId) {
      R.id.menu_action_search -> {
        findNavController(R.id.nav_host_fragment).navigate(R.id.searchFragment)
        item.expandActionView()
        return true
      }
      R.id.menu_action_logout -> {
        AuthUI.getInstance().signOut(this).addOnCompleteListener { finish() }
        return true
      }
    }
    return false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
      val response = IdpResponse.fromResultIntent(data)
      if (resultCode == RESULT_OK) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.mainFragment)
        return
      } else {

        if (response == null) {
          Snackbar.make(
            coordinatorLayout, resources.getString(R.string.sign_in_cancelled),
            Snackbar.LENGTH_LONG
          ).show()
          return
        }

        val error = response.error

        if (error != null) {
          if (error.errorCode == ErrorCodes.NO_NETWORK) {
            Snackbar.make(
              coordinatorLayout,
              resources.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG
            )
              .show()
            return
          }

          if (error.errorCode == ErrorCodes.UNKNOWN_ERROR) {
            Snackbar.make(
              coordinatorLayout,
              resources.getString(R.string.unknown_signin_error), Snackbar.LENGTH_LONG
            )
              .show()
            return
          }
        }
      }

      Snackbar.make(
        coordinatorLayout, resources.getString(R.string.sign_in_failed),
        Snackbar.LENGTH_LONG
      ).show()
    }
  }

  override fun onBookSelected(bookKey: String) {
    val bookActivityArgs = bundleOf(EXTRA_KEY to bookKey)
    findNavController(R.id.nav_host_fragment).navigate(R.id.bookActivity, bookActivityArgs)
  }

  override fun onBookReleased(bookKey: String) {
    findNavController(R.id.nav_host_fragment).popBackStack()
    onBookSelected(bookKey)
  }

  override fun onBookAdd() {
    findNavController(R.id.nav_host_fragment).navigate(R.id.bookReleaseFragment)
  }
}
