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

import com.bookcrossing.mobile.models.BookUri
import com.bookcrossing.mobile.util.ValidationResult.Invalid
import com.bookcrossing.mobile.util.ValidationResult.OK
import org.junit.Assert.assertTrue
import org.junit.Test

class BookUriSchemeRuleTest {
  private val rule = BookUriSchemeRule()

  @Test
  fun `correct scheme`() {
    val bookUri = BookUri(null, "bookcrossing", null, null)
    assertTrue(rule.check(bookUri) is OK)
  }

  @Test
  fun `case insensitive scheme`() {
    val bookUri = BookUri(PACKAGE_NAME.toUpperCase(), "BOOKCROSSING", null, null)
    assertTrue(rule.check(bookUri) is OK)
  }

  @Test
  fun `incorrect scheme`() {
    val bookUri = BookUri(null, "test", null, null)
    assertTrue(rule.check(bookUri) is Invalid)
  }

  @Test
  fun `null scheme`() {
    val bookUri = BookUri(null, null, null, null)
    assertTrue(rule.check(bookUri) is Invalid)
  }
}