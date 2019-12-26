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

package com.bookcrossing.mobile.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.presenters.MainPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.adapters.BooksAdapter
import com.bookcrossing.mobile.util.adapters.BooksViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions.Builder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import moxy.presenter.InjectPresenter

class MainFragment : BaseFragment(), MainView {

  @BindView(R.id.books_rv)
  lateinit var rv: RecyclerView

  @BindView(R.id.addBookButton)
  lateinit var fab: FloatingActionButton

  @BindView(R.id.adView)
  lateinit var ad: AdView

  @InjectPresenter
  lateinit var presenter: MainPresenter

  private lateinit var adapter: FirebaseRecyclerAdapter<Book, BooksViewHolder>

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_main, container, false)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    adapter.stopListening()
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    setupBookList()

    subscriptions.add(fab.clicks().subscribe { listener.onBookAdd() })

    loadAds()
  }

  private fun loadAds() {
    val adBuilder = AdRequest.Builder()
    presenter.checkForConsent(adBuilder)
    val adRequest = adBuilder.build()
    ad.loadAd(adRequest)
  }

  private fun setupBookList() {
    rv.layoutManager = LinearLayoutManager(activity)
    adapter = BooksAdapter(
      Builder<Book>().setQuery(presenter.books, Book::class.java)
        .setLifecycleOwner(viewLifecycleOwner)
        .build()
    )

    rv.adapter = adapter
    adapter.startListening()
  }
}

