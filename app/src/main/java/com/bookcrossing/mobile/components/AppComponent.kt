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

package com.bookcrossing.mobile.components

import android.content.Context
import com.bookcrossing.mobile.modules.ApiModule
import com.bookcrossing.mobile.modules.PrefModule
import com.bookcrossing.mobile.modules.ProvidersModule
import com.bookcrossing.mobile.ui.acquire.BookAcquireActivity
import com.bookcrossing.mobile.ui.bookpreview.BookActivity
import com.bookcrossing.mobile.ui.main.MainFragment
import com.bookcrossing.mobile.ui.map.MapsFragment
import com.bookcrossing.mobile.ui.profile.ProfileFragment
import com.bookcrossing.mobile.ui.releasebook.BookReleaseFragment
import com.bookcrossing.mobile.ui.releasebook.ReleaseAcquiredBookFragment
import com.bookcrossing.mobile.ui.scan.ScanActivity
import com.bookcrossing.mobile.ui.search.SearchBooksFragment
import com.bookcrossing.mobile.ui.stash.StashFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
@Singleton
@Component(modules = [PrefModule::class, ApiModule::class, ProvidersModule::class])
interface AppComponent {
  @Component.Factory
  interface Factory {
    fun create(@BindsInstance context: Context): AppComponent
  }

  fun inject(scanActivity: ScanActivity)

  fun inject(bookActivity: BookActivity)

  fun inject(bookAcquireActivity: BookAcquireActivity)

  fun inject(mainFragment: MainFragment)

  fun inject(profileFragment: ProfileFragment)

  fun inject(stashFragment: StashFragment)

  fun inject(searchFragment: SearchBooksFragment)

  fun inject(bookReleaseFragment: BookReleaseFragment)

  fun inject(releaseAcquiredBookFragment: ReleaseAcquiredBookFragment)

  fun inject(mapsFragment: MapsFragment)
}