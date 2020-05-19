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
package com.bookcrossing.mobile.ui.stash

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import butterknife.BindView
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.StashPresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.adapters.StashAdapter
import com.bookcrossing.mobile.util.adapters.StashedBookViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen to display all the books that user stashed for later
 * <p>
 * "Stashed" book means that user can see when this book went free or when somebody acquires it
 */
class StashFragment : BaseFragment(), StashView {
  @Inject
  lateinit var presenterProvider: Provider<StashPresenter>

  private val presenter: StashPresenter by moxyPresenter { presenterProvider.get() }

  @BindView(R.id.stash_rv)
  lateinit var rv: RecyclerView

  private lateinit var adapter: FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder>

  override fun onAttach(context: Context) {
    injector.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_stash, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    if (presenter.isAuthenticated) {
      setupStash()
    } else {
      authenticate()
      // TODO setup stash after successful auth
    }
  }

  override fun onDestroyView() {
    rv.adapter = null
    super.onDestroyView()
  }

  private fun setupStash() {
    val gridLayoutManager: LayoutManager =
      GridLayoutManager(activity, STASH_COLUMNS)
    rv.layoutManager = gridLayoutManager
    adapter = StashAdapter(
      presenter.bookCoverResolver,
      FirebaseRecyclerOptions.Builder<Boolean>().setQuery(
        presenter.stashedBooks,
        Boolean::class.java
      ).setLifecycleOwner(viewLifecycleOwner).build()
    )
    rv.adapter = adapter
    adapter.startListening()
  }

  companion object {
    const val STASH_COLUMNS = 3
  }
}