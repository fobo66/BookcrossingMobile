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

import com.bookcrossing.mobile.ui.scan.BookCodeAnalyzer
import com.bookcrossing.mobile.ui.scan.`ScanView$$State`
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ScanPresenterTest {
  private lateinit var state: `ScanView$$State`
  private lateinit var presenter: ScanPresenter

  @Before
  fun setUp() {
    state = mockk(relaxed = true)
    val bookCodeAnalyzer: BookCodeAnalyzer = mockk()
    presenter = ScanPresenter(bookCodeAnalyzer)
    presenter.setViewState(state)
  }

  @Test
  fun testValidUrl() {
    presenter.checkBookcrossingUri("bookcrossing://com.bookcrossing.mobile/book?key=123")
    verify {
      state.onBookCodeScanned(any())
    }
  }

  @Test
  fun testInvalidUrl() {
    presenter.checkBookcrossingUri("https://example.com")
    verify {
      state.onIncorrectCodeScanned()
    }
  }
}