package com.bookcrossing.mobile.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import butterknife.BindView
import butterknife.ButterKnife
import com.algolia.instantsearch.helpers.InstantSearch
import com.algolia.instantsearch.helpers.Searcher
import com.algolia.instantsearch.ui.views.Hits
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.util.Constants
import com.bookcrossing.mobile.util.listeners.BookListener
import com.crashlytics.android.Crashlytics
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
import org.json.JSONException
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

  @BindView(R.id.hits)
  lateinit var hits: Hits

  private lateinit var instantSearch: InstantSearch

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)

    setupToolbar()
    setupSearch()

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
          .build(), Constants.RC_SIGN_IN
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
              privacyUrl = URL(Constants.PRIVACY_POLICY_URL)
            } catch (e: MalformedURLException) {
              throw RuntimeException(e)
            }

            val form = ConsentForm.Builder(this@MainActivity, privacyUrl).withListener(
              object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                  Log.d(TAG, "onConsentFormLoaded: Consent form loaded successfully.")
                }

                override fun onConsentFormOpened() {
                  Log.d(TAG, "onConsentFormOpened: Consent form was displayed.")
                }

                override fun onConsentFormClosed(
                  consentStatus: ConsentStatus?,
                  userPrefersAdFree: Boolean?
                ) {
                  Log.d(TAG, "onConsentFormClosed: " + consentStatus!!)
                  saveConsentStatus(consentStatus)
                }

                override fun onConsentFormError(errorDescription: String?) {
                  Log.d(
                    TAG,
                    "User's consent status failed to update: $errorDescription"
                  )
                  Crashlytics.log(errorDescription)
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
        Log.d(TAG, "User's consent status failed to update: $errorDescription")
      }
    })
  }

  private fun saveConsentStatus(consentStatus: ConsentStatus) {
    PreferenceManager.getDefaultSharedPreferences(applicationContext)
      .edit()
      .putString(Constants.KEY_CONSENT_STATUS, consentStatus.toString())
      .apply()
  }

  private fun resolveNavigationToFragment(savedInstanceState: Bundle?) {
    val navController = findNavController(R.id.nav_host_fragment)
    if (intent != null) {
      val whereToGo = intent.getStringExtra(Constants.EXTRA_TARGET_FRAGMENT)
      when {
        whereToGo != null -> when {
          "BookCreateFragment".equals(
            whereToGo,
            ignoreCase = true
          ) -> navController.navigate(R.id.bookCreateFragment)
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

  private fun setupSearch() {
    val searcher = Searcher.create(
      getString(R.string.algolia_app_id), getString(R.string.algolia_api_key),
      getString(R.string.algolia_index_name)
    )
    instantSearch = InstantSearch(hits, searcher)
    instantSearch.registerSearchView(this, toolbar.menu, R.id.menu_action_search)
    setupSearchHits()
  }

  private fun setupSearchHits() {
    hits.setOnItemClickListener { _, position, _ ->
      try {
        hits.visibility = View.GONE
        onBookSelected(hits.get(position).getString("objectID"))
      } catch (e: JSONException) {
        Snackbar.make(coordinatorLayout, "Cannot open book info", Snackbar.LENGTH_SHORT).show()
        e.printStackTrace()
      }
    }
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.menu_action_search -> {
        if (hits.visibility == View.GONE) {
          hits.visibility = View.VISIBLE
        } else {
          hits.visibility = View.GONE
        }
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
    if (requestCode == Constants.RC_SIGN_IN) {
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
    val bookActivityArgs = bundleOf(Constants.EXTRA_KEY to bookKey)
    findNavController(R.id.nav_host_fragment).navigate(R.id.bookActivity, bookActivityArgs)
  }

  override fun onBookReleased(bookKey: String) {
    findNavController(R.id.nav_host_fragment).popBackStack()
    onBookSelected(bookKey)
  }

  override fun onBookAdd() {
    hits.visibility = View.GONE
    findNavController(R.id.nav_host_fragment).navigate(R.id.bookCreateFragment)
  }

  override fun setTitle(@StringRes fragmentTitleId: Int) {
    toolbar.setTitle(fragmentTitleId)
  }

  companion object {

    private const val TAG = "MainActivity"
  }
}
