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
package com.bookcrossing.mobile.ui.bookpreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.ActivityNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.drawable
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Coordinates
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.BookPresenter
import com.bookcrossing.mobile.ui.acquire.BookAcquireActivity
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.ui.map.BookLocationBottomSheet
import com.bookcrossing.mobile.util.DEFAULT_DEBOUNCE_TIMEOUT
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.bookcrossing.mobile.util.adapters.PlacesHistoryViewHolder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions.Builder
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dev.chrisbanes.insetter.ViewState
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import moxy.ktx.moxyPresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Provider

/** Book details screen */
class BookActivity : BaseActivity(), BookView,
  OnMenuItemClickListener {

  @Inject
  lateinit var presenterProvider: Provider<BookPresenter>

  private val presenter: BookPresenter by moxyPresenter { presenterProvider.get() }

  @BindView(id.book_activity_root)
  lateinit var root: CoordinatorLayout

  @BindView(id.toolbar)
  lateinit var toolbar: Toolbar

  @BindView(id.toolbar_container)
  lateinit var toolbarContainer: AppBarLayout

  @BindView(id.collapsing_toolbar_container)
  lateinit var collapsingToolbarContainer: CollapsingToolbarLayout

  @BindView(id.cover)
  lateinit var cover: ImageView

  @BindView(id.nestedScrollView)
  lateinit var nestedScrollView: NestedScrollView

  @BindView(id.author)
  lateinit var author: TextView

  @BindView(id.positionName)
  lateinit var position: TextView

  @BindView(id.book_desc)
  lateinit var description: TextView

  @BindView(id.timestamp)
  lateinit var wentFree: RelativeTimeTextView

  @BindView(id.placesHistory)
  lateinit var placesHistory: RecyclerView

  @BindView(id.acquire_button)
  lateinit var acquireButton: Button

  @BindView(id.fab_like)
  lateinit var favorite: FloatingActionButton

  private lateinit var key: String
  private lateinit var adapter: FirebaseRecyclerAdapter<Coordinates, PlacesHistoryViewHolder>

  private var currentBookPosition: Coordinates? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    injector.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_book)
    ButterKnife.bind(this)

    root.systemUiVisibility =
      View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    setupToolbar()
    setupInsets()

    if (intent != null) {
      key = intent.getStringExtra(EXTRA_KEY).orEmpty()
    }
    setupPlacesHistory()

    subscriptions.add(
      favorite.clicks()
        .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .flatMap { presenter.handleBookStashing(key) }
        .subscribe({}, Timber::e)
    )
    subscriptions.add(
      acquireButton.clicks()
        .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .subscribe { handleAcquiring() }
    )
    subscriptions.add(
      position.clicks()
        .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .subscribe({
          BookLocationBottomSheet.newInstance(currentBookPosition)
            .show(supportFragmentManager, BookLocationBottomSheet.TAG)
        }, Timber::e)
    )
  }

  override fun onResume() {
    super.onResume()

    presenter.loadBook(key)
    presenter.checkStashingState(key)
  }

  override fun finish() {
    super.finish()
    ActivityNavigator.applyPopAnimationsToPendingTransition(this)
  }

  private fun setupToolbar() = toolbar.apply {
    inflateMenu(R.menu.menu_book)
    setOnMenuItemClickListener(this@BookActivity)
    setNavigationIcon(drawable.ic_back)
    setNavigationOnClickListener { onBackPressed() }
  }

  private fun setupInsets() {
    toolbarContainer.doOnApplyWindowInsets { view: View, windowInsets: WindowInsetsCompat, initial: ViewState ->
      view.setPadding(
        initial.paddings.left,
        windowInsets.systemWindowInsetTop + initial.paddings.top,
        initial.paddings.right, initial.paddings.bottom
      )
    }
    cover.doOnApplyWindowInsets { view: View, windowInsets: WindowInsetsCompat, initial: ViewState ->
      val params = view.layoutParams as MarginLayoutParams
      params.topMargin = windowInsets.systemWindowInsetTop + initial.margins.top
      view.layoutParams = params
    }
    nestedScrollView.doOnApplyWindowInsets { view: View, windowInsets: WindowInsetsCompat, initial: ViewState ->
      view.setPadding(
        initial.paddings.left,
        initial.paddings.top, initial.paddings.right,
        windowInsets.systemWindowInsetBottom + initial.paddings.bottom
      )
    }
    favorite.doOnApplyWindowInsets { view: View, windowInsets: WindowInsetsCompat, initialPadding: ViewState ->
      view.setPadding(
        initialPadding.paddings.left, initialPadding.paddings.top,
        windowInsets.systemWindowInsetRight + initialPadding.paddings.right,
        initialPadding.paddings.bottom
      )
    }
  }

  private fun setupPlacesHistory() {
    placesHistory.layoutManager = LinearLayoutManager(this)
    adapter = object : FirebaseRecyclerAdapter<Coordinates, PlacesHistoryViewHolder>(
      Builder<Coordinates>().setQuery(
        presenter.getPlacesHistory(key),
        Coordinates::class.java
      ).setLifecycleOwner(this).build()
    ) {
      override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
      ): PlacesHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
          .inflate(layout.places_history_list_item, parent, false)
        return PlacesHistoryViewHolder(view)
      }

      override fun onBindViewHolder(
        holder: PlacesHistoryViewHolder, position: Int,
        model: Coordinates
      ) {
        holder.bind(getRef(position).key, model)
      }
    }
    placesHistory.adapter = adapter
    adapter.startListening()
  }

  override fun onMenuItemClick(item: MenuItem): Boolean {
    if (item.itemId == id.menu_action_report) {
      presenter.reportAbuse(key)
      return true
    }
    return false
  }

  private fun handleAcquiring() {
    val acquireIntent = Intent(this, BookAcquireActivity::class.java)
    acquireIntent.putExtra(getString(string.extra_insideAppRequest), true)
    startActivity(acquireIntent)
  }

  override fun onBookLoaded(book: Book) {
    collapsingToolbarContainer.title = book.name
    GlideApp.with(this)
      .load(presenter.bookCoverResolver.resolveCover(key))
      .transition(DrawableTransitionOptions.withCrossFade())
      .thumbnail(0.6f)
      .into(cover)
    author.text = book.author
    position.text = getString(string.book_place_template, book.city, book.positionName)
    wentFree.setReferenceTime(book.wentFreeAt.timestamp)
    description.text = book.description

    acquireButton.visibility = if (book.isFree) {
      View.VISIBLE
    } else {
      View.GONE
    }
    currentBookPosition = book.position
  }

  override fun onErrorToLoadBook() {
    MaterialDialog(this).show {
      message(string.failed_to_load_book_message)
      title(string.error_dialog_title)
      positiveButton(
        string.ok
      ) {
        onBackPressed()
      }
    }
  }

  override fun onBookStashed() {
    favorite.setImageResource(drawable.ic_turned_in_white_24dp)
  }

  override fun onBookUnstashed() {
    favorite.setImageResource(drawable.ic_turned_in_not_white_24dp)
  }

  override fun onAbuseReported() {
    Snackbar.make(root, string.report_abuse_success, Snackbar.LENGTH_SHORT).show()
  }

  companion object {

    /** Create Intent to start BookActivity */
    fun getStartIntent(context: Context, key: String): Intent =
      Intent(context, BookActivity::class.java)
        .putExtra(EXTRA_KEY, key)
  }
}