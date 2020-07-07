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

package com.bookcrossing.mobile.interactor

import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.util.BookUriAuthorityRule
import com.bookcrossing.mobile.util.BookUriCodeRule
import com.bookcrossing.mobile.util.BookUriPathRule
import com.bookcrossing.mobile.util.BookUriSchemeRule
import com.bookcrossing.mobile.util.TestBookUriProvider
import com.bookcrossing.mobile.util.ValidationResult.Invalid
import com.bookcrossing.mobile.util.ValidationResult.OK
import com.bookcrossing.mobile.util.Validator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookCodeInteractorTest {

  private lateinit var interactor: BookCodeInteractor

  @Before
  fun setUp() {
    interactor = BookCodeInteractor(
      TestBookUriProvider(), Validator(
        BookUriAuthorityRule(),
        BookUriSchemeRule(),
        BookUriPathRule(),
        BookUriCodeRule()
      )
    )
  }

  @Test
  fun `URI build by app is valid`() {
    val uri = interactor.buildBookUri("test")
    println(uri)
    assertTrue(interactor.checkBookcrossingUri(uri) is OK)
  }

  @Test
  fun `random URI is not valid`() {
    val uri = "https://example.com"
    assertTrue(interactor.checkBookcrossingUri(uri) is Invalid)
  }

  @Test
  fun `error message for invalid URI`() {
    val uri = "https://example.com"
    val result = interactor.checkBookcrossingUri(uri) as Invalid
    assertEquals(string.incorrect_code_scanned_message, result.messageId)
  }

  @Test
  fun `same message for different URI issues`() {
    val wrongAuthorityUri = "bookcrossing://com.test/book?key=test"
    val wrongAuthorityResult = interactor.checkBookcrossingUri(wrongAuthorityUri) as Invalid
    val wrongSchemeUri = "bookcrossing://com.test/book?key=test"
    val wrongSchemeResult = interactor.checkBookcrossingUri(wrongSchemeUri) as Invalid
    assertEquals(wrongSchemeResult.messageId, wrongAuthorityResult.messageId)
  }
}