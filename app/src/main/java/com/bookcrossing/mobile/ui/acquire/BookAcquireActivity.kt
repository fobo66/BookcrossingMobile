/*
 *    Copyright 2020 Andrey Mukamolov
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
package com.bookcrossing.mobile.ui.acquire

import android.content.Intent
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R.id
import com.bookcrossing.mobile.R.layout
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.models.BookCode
import com.bookcrossing.mobile.models.BookCode.CorrectCode
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.BookAcquirePresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.ui.scan.ScanActivity
import com.bookcrossing.mobile.util.DEFAULT_DEBOUNCE_TIMEOUT
import com.bookcrossing.mobile.util.EXTRA_KEY
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import io.reactivex.Observable
import io.reactivex.rxkotlin.withLatestFrom
import moxy.ktx.moxyPresenter
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen for acquiring book
 */
class BookAcquireActivity : BaseActivity(), BookAcquireView {

  @Inject
  lateinit var presenterProvider: Provider<BookAcquirePresenter>

  private val presenter: BookAcquirePresenter by moxyPresenter { presenterProvider.get() }

  @BindView(id.toolbar)
  lateinit var toolbar: Toolbar

  @BindView(id.book_acquire_toolbar_container)
  lateinit var toolbarContainer: AppBarLayout

  @BindView(id.acquireButton)
  lateinit var acquireButton: Button

  @BindView(id.scan_code)
  lateinit var scanCodeButton: Button

  @BindView(id.input_code)
  lateinit var codeInput: TextInputEditText

  @BindView(id.coord_layout)
  lateinit var coordinatorLayout: CoordinatorLayout

  private var keyToAcquire: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    injector.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_book_acquire)
    ButterKnife.bind(this)
    toolbar.setNavigationOnClickListener { onBackPressed() }
    if (intent != null) {
      keyToAcquire = intent.data?.getQueryParameter(EXTRA_KEY)
      val isInnerAppRequest =
        intent.getBooleanExtra(getString(string.extra_insideAppRequest), false)
      if (!isInnerAppRequest) {
        codeInput.setText(keyToAcquire)
        codeInput.isEnabled = false
      }
    }

    coordinatorLayout.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE or
      SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

    setupInsets()

    subscriptions.add(
      onAcquireButtonClicked()
        .withLatestFrom(
          codeInput.textChanges()
            .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        ) { _, code -> code.toString() }
        .flatMapSingle { presenter.validateCode(it) }
        .flatMap { code: BookCode ->
          if (code is CorrectCode) {
            presenter.processBookAcquisition(code.code)
              .andThen(Observable.just<BookCode>(code))
          } else {
            Observable.just(code)
          }
        }
        .subscribe { code: BookCode ->
          presenter.handleAcquisitionResult(
            code
          )
        }
    )

    subscriptions.add(
      scanCodeButton.clicks()
        .doOnNext { disableButtons() }
        .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .subscribe {
          launchCodeScanner()
        }
    )
  }

  private fun disableButtons() {
    acquireButton.isEnabled = false
    scanCodeButton.isEnabled = false
  }

  override fun onIncorrectKey() {
    Snackbar.make(coordinatorLayout, string.incorrect_key_message, Snackbar.LENGTH_SHORT).show()

    acquireButton.isEnabled = true
    scanCodeButton.isEnabled = true
  }

  override fun onAcquired() {
    Snackbar.make(coordinatorLayout, string.book_acquired_success_message, Snackbar.LENGTH_SHORT)
      .show()
    onBackPressed()
  }

  private fun launchCodeScanner() {
    val intent = Intent(this, ScanActivity::class.java)
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
    ContextCompat.startActivity(this, intent, options)
  }

  private fun setupInsets() {
    toolbarContainer.doOnApplyWindowInsets { view, windowInsets, initialState ->
      view.setPadding(
        initialState.paddings.left,
        windowInsets.systemWindowInsetTop + initialState.paddings.top,
        initialState.paddings.right, initialState.paddings.bottom
      )
    }

    acquireButton.doOnApplyWindowInsets { view, insets, initialState ->
      view.updateLayoutParams<MarginLayoutParams> {
        bottomMargin = initialState.margins.bottom + insets.systemWindowInsetBottom
      }
    }
  }

  private fun onAcquireButtonClicked(): Observable<Unit> {
    return acquireButton.clicks()
      .doOnNext { disableButtons() }
      .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
  }
}