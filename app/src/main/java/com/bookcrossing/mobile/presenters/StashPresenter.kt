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

import com.bookcrossing.mobile.ui.stash.StashView
import com.bookcrossing.mobile.util.DEFAULT_USER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import moxy.InjectViewState
import javax.inject.Inject

/**
 * Presenter fo stash screen
 */
@InjectViewState
class StashPresenter @Inject constructor(
  private val database: FirebaseDatabase,
  private val auth: FirebaseAuth
) : BasePresenter<StashView>() {

  val stashedBooks: DatabaseReference
    get() = database.getReference("stash")
      .child(auth.currentUser?.uid ?: DEFAULT_USER)
}
