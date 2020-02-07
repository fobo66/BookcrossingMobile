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
package com.bookcrossing.mobile.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.drawable
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.modules.App
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.presenters.ProfilePresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.RC_SIGN_IN
import com.bookcrossing.mobile.util.adapters.AcquiredBooksAdapter
import com.bookcrossing.mobile.util.adapters.AcquiredBooksViewHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions.Builder
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen where user can manage all his/her acquired books
 */
class ProfileFragment : BaseFragment(), ProfileView {

  @Inject
  lateinit var presenterProvider: Provider<ProfilePresenter>

  private val presenter: ProfilePresenter by moxyPresenter { presenterProvider.get() }

  @BindView(R.id.profile_image)
  lateinit var profileImage: ImageView
  @BindView(R.id.acquiredBooksList)
  lateinit var acquiredBooksList: RecyclerView
  @BindView(R.id.acquiredBooksListEmptyIndicator)
  lateinit var acquiredBooksListEmptyIndicator: AppCompatTextView

  private lateinit var adapter: FirebaseRecyclerAdapter<Book, AcquiredBooksViewHolder>

  override fun onCreate(savedInstanceState: Bundle?) {
    App.getComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(layout.fragment_profile, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    if (presenter.isAuthenticated) {
      setupAcquiredBookList()
      loadProfileInfo()
    } else {
      authenticate()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
      setupAcquiredBookList()
      loadProfileInfo()
    }
  }

  private fun loadProfileInfo() {
    adapter.startListening()
    GlideApp.with(this)
      .load(presenter.photoUrl)
      .placeholder(drawable.ic_account_circle_black_24dp)
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(profileImage)
  }

  private fun setupAcquiredBookList() {
    acquiredBooksList.layoutManager = LinearLayoutManager(requireContext())
    adapter = AcquiredBooksAdapter(
      Builder<Book>().setQuery(
        presenter.acquiredBooks,
        Book::class.java
      )
        .setLifecycleOwner(viewLifecycleOwner)
        .build()
    )
    acquiredBooksList.adapter = adapter
  }
}