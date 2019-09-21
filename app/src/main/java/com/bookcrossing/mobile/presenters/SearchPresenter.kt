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

package com.bookcrossing.mobile.presenters

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.searcher.Searcher
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.client.ClientSearch
import com.algolia.search.client.Index
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.SearchHitBook
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.ui.search.SearchHitBooksAdapter
import com.bookcrossing.mobile.ui.search.SearchView
import com.bookcrossing.mobile.util.ResourceProvider
import moxy.InjectViewState
import javax.inject.Inject

/**
 * (c) 2019 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 2019-09-15.
 */
@InjectViewState
class SearchPresenter : BasePresenter<SearchView>() {

  val searchableBooks: LiveData<PagedList<SearchHitBook>>
  val adapter: SearchHitBooksAdapter
  val searchBox: SearchBoxConnectorPagedList<ResponseSearch>

  private val client: ClientSearch
  private val index: Index
  private val searcher: Searcher<ResponseSearch>
  private val connection = ConnectionHandler()

  @Inject
  lateinit var resourceProvider: ResourceProvider

  init {
    App.getComponent().inject(this)

    client = ClientSearch(
      ApplicationID(resourceProvider.getString(string.algolia_app_id)),
      APIKey(resourceProvider.getString(string.algolia_api_key))
    )
    index = client.initIndex(IndexName(resourceProvider.getString(string.algolia_index_name)))
    searcher = SearcherSingleIndex(index)

    val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) { hit ->
      hit.deserialize(SearchHitBook.serializer())
    }
    val pagedListConfig = PagedList.Config.Builder().setPageSize(50).build()

    searchableBooks =
      LivePagedListBuilder(dataSourceFactory, pagedListConfig)
        .build()

    adapter = SearchHitBooksAdapter()

    searchBox = SearchBoxConnectorPagedList(searcher, listOf(searchableBooks))
    connection += searchBox
  }

  override fun onDestroy() {
    super.onDestroy()
    searcher.cancel()
    connection.disconnect()
  }

}