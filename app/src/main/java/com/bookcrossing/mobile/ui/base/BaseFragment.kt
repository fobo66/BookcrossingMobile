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
package com.bookcrossing.mobile.ui.base

import android.content.Context
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bookcrossing.mobile.util.BookListenerDelegate
import com.bookcrossing.mobile.util.RC_SIGN_IN
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment

/**
 * Base class for fragments
 * Created by fobo66 on 16.11.2016.
 */
abstract class BaseFragment : MvpAppCompatFragment() {
  protected lateinit var unbinder: Unbinder
  protected var subscriptions = CompositeDisposable()
  protected lateinit var listener: BookListenerDelegate

  override fun onAttach(context: Context) {
    super.onAttach(context)
    listener = BookListenerDelegate(context)
  }

  override fun onDetach() {
    super.onDetach()
    listener.detachListener()
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    unbinder = ButterKnife.bind(this, view)
  }

  protected fun authenticate() {
    startActivityForResult(
      AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(listOf(GoogleBuilder().build()))
        .build(), RC_SIGN_IN
    )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    unbinder.unbind()
    subscriptions.clear()
  }
}