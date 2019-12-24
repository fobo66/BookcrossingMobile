/*
 *    Copyright 2017 Andrey Mukamolov
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
package com.bookcrossing.mobile.util.adapters

import android.view.View
import moxy.MvpDelegate

/**
 * Created by fobo66 on 09.05.17.
 */
open class MvpBaseViewHolder(view: View) : BaseViewHolder(view) {
  private val mvpDelegate: MvpDelegate<out BaseViewHolder> by lazy { MvpDelegate(this) }

  init {
    mvpDelegate.onCreate()
  }
}