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
import org.junit.Assert.assertEquals
import org.junit.Test

class NotEmptyRuleTest {

  private val rule = NotEmptyRule()

  @Test
  fun notEmpty_OK() {
    assert(rule.check("test") is OK)
  }

  @Test
  fun emptyString_Invalid() {
    assert(rule.check("") is Invalid)
  }

  @Test
  fun blankString_Invalid() {
    assert(rule.check("   ") is Invalid)
  }

  @Test
  fun emptyString_Invalid_correctMessage() {
    val invalidResult: Invalid = rule.check("") as Invalid
    assertEquals(R.string.error_input_empty, invalidResult.messageId)
  }

}