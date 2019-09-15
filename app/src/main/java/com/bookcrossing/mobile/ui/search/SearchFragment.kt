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

package com.bookcrossing.mobile.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.presenters.SearchPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment

class SearchFragment : BaseFragment(), SearchView {

  @InjectPresenter
  lateinit var presenter: SearchPresenter

  @BindView(R.id.search_hits)
  lateinit var searchHits: RecyclerView

  @BindView(R.id.toolbar)
  lateinit var toolbar: Toolbar

  private val connection = ConnectionHandler()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_search_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    presenter.searchableBooks.observe(this, Observer { hits ->
      presenter.adapter.submitList(hits)
    })

    searchHits.apply {
      adapter = presenter.adapter
      layoutManager = LinearLayoutManager(requireContext())
      autoScrollToStart(presenter.adapter)
    }

    val searchBoxView =
      SearchBoxViewAppCompat(toolbar.menu.getItem(R.id.menu_action_search).actionView as androidx.appcompat.widget.SearchView)
    connection += presenter.searchBox.connectView(searchBoxView)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    connection.disconnect()
  }
}
