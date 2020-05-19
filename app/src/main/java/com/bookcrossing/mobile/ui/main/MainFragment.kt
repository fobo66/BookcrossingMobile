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

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.MainPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.RC_SIGN_IN
import com.bookcrossing.mobile.util.adapters.BooksAdapter
import com.bookcrossing.mobile.util.adapters.BooksViewHolder
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions.Builder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import moxy.ktx.moxyPresenter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class MainFragment : BaseFragment(), MainView {

  @BindView(R.id.books_rv)
  lateinit var rv: RecyclerView

  @BindView(R.id.addBookButton)
  lateinit var fab: FloatingActionButton

  @BindView(R.id.adView)
  lateinit var ad: AdView

  @Inject
  lateinit var presenterProvider: Provider<MainPresenter>

  private val presenter: MainPresenter by moxyPresenter { presenterProvider.get() }

  private lateinit var adapter: FirebaseRecyclerAdapter<Book, BooksViewHolder>

  override fun onAttach(context: Context) {
    injector.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_main, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    if (!presenter.isAuthenticated) {
      fab.visibility = GONE
      authenticate()
    }

    setupBookList()

    subscriptions.add(fab.clicks().subscribe { listener.onBookAdd() })

    loadAds()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == RC_SIGN_IN) {
      val signInResult = IdpResponse.fromResultIntent(data)

      if (resultCode == RESULT_OK) {
        Snackbar.make(rv, R.string.sign_in_success, Snackbar.LENGTH_LONG).show()
      } else {
        if (signInResult == null) {
          Snackbar.make(
            rv, resources.getString(R.string.sign_in_cancelled),
            Snackbar.LENGTH_LONG
          ).show()
          return
        }

        val error = signInResult.error

        Timber.e(error, "Sign in failed")

        when (error?.errorCode) {
          ErrorCodes.NO_NETWORK -> {
            Snackbar.make(
              rv,
              resources.getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG
            )
              .show()
          }
          ErrorCodes.UNKNOWN_ERROR -> {
            Snackbar.make(
              rv,
              resources.getString(R.string.unknown_signin_error), Snackbar.LENGTH_LONG
            )
              .show()
          }
        }
      }
    }
  }

  private fun loadAds() {
    val adBuilder = AdRequest.Builder()
    presenter.checkForConsent(adBuilder)
    val adRequest = adBuilder.build()
    ad.loadAd(adRequest)
  }

  private fun setupBookList() {
    rv.layoutManager = LinearLayoutManager(requireContext())
    adapter = BooksAdapter(
      presenter.bookCoverResolver,
      Builder<Book>().setQuery(presenter.books, Book::class.java)
        .setLifecycleOwner(viewLifecycleOwner)
        .build()
    )

    rv.adapter = adapter
  }

  override fun onDestroyView() {
    rv.adapter = null
    ad.adListener = null
    super.onDestroyView()
  }

  override fun showReleaseBookButton() {
    fab.visibility = VISIBLE
  }
}