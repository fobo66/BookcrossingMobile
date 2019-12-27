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

package com.bookcrossing.mobile.ui.releasebook

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.github.florent37.runtimepermission.rx.RxPermissions

class ReleaseAcquiredBookFragment : BaseFragment(), ReleaseAcquiredBookView {

  private lateinit var permissions: RxPermissions

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_book_release_acquired, container, false)
  }

  override fun onCoverChosen(coverUri: Uri?) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun showCover() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onReleased(newKey: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onFailedToRelease() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun askUserToProvideDefaultCity() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}