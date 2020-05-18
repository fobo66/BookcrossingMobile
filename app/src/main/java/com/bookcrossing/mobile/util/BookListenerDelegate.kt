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

package com.bookcrossing.mobile.util

import android.content.Context
import com.bookcrossing.mobile.util.listeners.BookListener

/**
 * Delegate to nicely connect MainActivity and fragments inside it
 */
class BookListenerDelegate(context: Context) : BookListener {
  private var listener: BookListener? = null

  /** Disconnect delegate from activity to prevent memory leaks */
  fun detachListener() {
    listener = null
  }

  override fun onBookSelected(bookKey: String) {
    listener?.onBookSelected(bookKey)
  }

  override fun onBookReleased(bookKey: String) {
    listener?.onBookReleased(bookKey)
  }

  override fun onBookAdd() {
    listener?.onBookAdd()
  }

  init {
    listener = if (context is BookListener) {
      context
    } else {
      throw IllegalStateException("$context must implement BookListener")
    }
  }
}