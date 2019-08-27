package com.bookcrossing.mobile.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import butterknife.BindView
import butterknife.ButterKnife
import com.algolia.instantsearch.helpers.InstantSearch
import com.algolia.instantsearch.helpers.Searcher
import com.algolia.instantsearch.ui.views.Hits
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.util.Constants
import com.bookcrossing.mobile.util.NavigationDrawerResolver
import com.bookcrossing.mobile.util.listeners.BookListener
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.ads.consent.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import java.net.MalformedURLException
import java.net.URL

class MainActivity : BaseActivity(), BookListener, NavigationView.OnNavigationItemSelectedListener {

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

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var instantSearch: InstantSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        setupSearch()
        setupToolbar()

        navigationView.setNavigationItemSelectedListener(this)

        checkForConsent()

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            resolveNavigationToFragment(savedInstanceState)
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                            listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(), Constants.RC_SIGN_IN)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        drawerToggle = setupDrawerToggle()
        drawer.addDrawerListener(drawerToggle)
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

                                    override fun onConsentFormClosed(consentStatus: ConsentStatus?,
                                                                     userPrefersAdFree: Boolean?) {
                                        Log.d(TAG, "onConsentFormClosed: " + consentStatus!!)
                                        saveConsentStatus(consentStatus)
                                    }

                                    override fun onConsentFormError(errorDescription: String?) {
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
        if (intent != null && savedInstanceState == null) {
            val whereToGo = intent.getStringExtra(Constants.EXTRA_TARGET_FRAGMENT)
            when {
                whereToGo != null -> when {
                    "BookCreateFragment".equals(whereToGo, ignoreCase = true) -> findNavController(R.id.nav_host_fragment).navigate(R.id.bookCreateFragment)
                    "ProfileFragment".equals(whereToGo, ignoreCase = true) -> findNavController(R.id.nav_host_fragment).navigate(R.id.profileFragment)
                    else -> findNavController(R.id.nav_host_fragment).navigate(R.id.mainFragment)
                }
                else -> findNavController(R.id.nav_host_fragment).navigate(R.id.mainFragment)
            }
        } else {
            findNavController(R.id.nav_host_fragment).navigate(R.id.mainFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        instantSearch.registerSearchView(this, menu, R.id.menu_action_search)
        return true
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open,
                R.string.drawer_close)
    }

    private fun setupSearch() {
        val searcher = Searcher.create(getString(R.string.algolia_app_id), getString(R.string.algolia_api_key),
                getString(R.string.algolia_index_name))
        instantSearch = InstantSearch(hits, searcher)

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

    private fun selectDrawerItem(@IdRes itemId: Int) {
        findNavController(R.id.nav_host_fragment).navigate(NavigationDrawerResolver.resolveNavigationDrawerItem(itemId))
        drawer.closeDrawer(navigationView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawer.openDrawer(GravityCompat.START)
                return true
            }
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
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectDrawerItem(item.itemId)
        return true
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
                    Snackbar.make(coordinatorLayout, resources.getString(R.string.sign_in_cancelled),
                            Snackbar.LENGTH_LONG).show()
                    return
                }

                val error = response.error

                if (error != null) {
                    if (error.errorCode == ErrorCodes.NO_NETWORK) {
                        Snackbar.make(coordinatorLayout,
                                resources.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
                                .show()
                        return
                    }

                    if (error.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                        Snackbar.make(coordinatorLayout,
                                resources.getString(R.string.unknown_signin_error), Snackbar.LENGTH_LONG)
                                .show()
                        return
                    }
                }
            }

            Snackbar.make(coordinatorLayout, resources.getString(R.string.sign_in_failed),
                    Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onBookSelected(bookKey: String) {
        val bookIntent = Intent(this, BookActivity::class.java)
        bookIntent.putExtra(Constants.EXTRA_KEY, bookKey)
        startActivity(bookIntent)
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
