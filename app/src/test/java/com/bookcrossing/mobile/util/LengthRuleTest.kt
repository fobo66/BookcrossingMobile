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
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.*
import org.junit.Test

class LengthRuleTest {

  private val rule = LengthRule(0, 10)

  @Test
  fun correctLength_OK() {
    assertThat(rule.check("test"), instanceOf(OK.javaClass))
  }

  @Test
  fun tooLong_Invalid() {
    assertThat(rule.check("testtesttesttesttest"), instanceOf(Invalid::class.java))
  }

  @Test
  fun tooLong_Invalid_correctMessage() {
    val invalidResult = rule.check("testtesttesttesttest") as Invalid
    assertEquals(R.string.error_too_long, invalidResult.messageId)
  }

  @Test
  fun empty_Invalid() {
    assertThat(rule.check(""), instanceOf(Invalid::class.java))
  }

  @Test
  fun empty_Invalid_correctMessage() {
    val invalidResult = rule.check("") as Invalid
    assertEquals(R.string.error_too_short, invalidResult.messageId)
  }
}
