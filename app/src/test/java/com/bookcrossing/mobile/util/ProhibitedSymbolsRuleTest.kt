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

import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.util.ValidationResult.Invalid
import com.bookcrossing.mobile.util.ValidationResult.OK
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class ProhibitedSymbolsRuleTest {
  private val rule = ProhibitedSymbolsRule()

  @Test
  fun plainText_OK() {
    assertThat(rule.check("test"), Matchers.instanceOf(OK.javaClass))
  }

  @Test
  fun defaultRegex_failedSymbol_Invalid() {
    assertThat(rule.check("test#"), Matchers.instanceOf(Invalid::class.java))
  }

  @Test
  fun customRegex_failedSymbol_Invalid() {
    val customRule = ProhibitedSymbolsRule("a".toRegex())
    assertThat(customRule.check("testa"), Matchers.instanceOf(Invalid::class.java))
  }

  @Test
  fun failedSymbol_Invalid_correctMessage() {
    val invalidResult: Invalid = rule.check("test#") as Invalid
    assertEquals(R.string.error_input_incorrect_symbols, invalidResult.messageId)
  }
}
