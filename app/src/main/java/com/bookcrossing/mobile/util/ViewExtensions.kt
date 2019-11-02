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

package com.bookcrossing.mobile.util

import android.os.Build.VERSION_CODES
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.WindowInsets
import androidx.annotation.RequiresApi

@RequiresApi(VERSION_CODES.KITKAT_WATCH)
fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
  // Create a snapshot of the view's padding state
  val initialPadding = recordInitialPaddingForView(this)
  // Set an actual OnApplyWindowInsetsListener which proxies to the given
  // lambda, also passing in the original padding state
  setOnApplyWindowInsetsListener { v, insets ->
    f(v, insets, initialPadding)
    // Always return the insets, so that children can also use them
    insets
  }
  // request some insets
  requestApplyInsetsWhenAttached()
}

data class InitialPadding(
  val left: Int, val top: Int,
  val right: Int, val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
  view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

@RequiresApi(VERSION_CODES.KITKAT_WATCH)
fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    // We're already attached, just request as normal
    requestApplyInsets()
  } else {
    // We're not attached to the hierarchy, add a listener to
    // request when we are
    addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}