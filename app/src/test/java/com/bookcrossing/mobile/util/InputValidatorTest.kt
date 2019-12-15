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
import org.junit.Test

/**
 * Test cases for validator with different rules
 */
class InputValidatorTest {

  @Test
  fun validator_noRules_OK() {
    val validator = InputValidator()
    assert(validator.validate("test") is OK)
  }

  @Test
  fun validator_emptyRule_OK() {
    val validator = InputValidator(NotEmptyRule())
    assert(validator.validate("test") is OK)
  }

  @Test
  fun validator_emptyRule_Invalid() {
    val validator = InputValidator(NotEmptyRule())
    assert(validator.validate("") is Invalid)
  }

  @Test
  fun validator_emptyRule_Invalid_correctMessage() {
    val validator = InputValidator(NotEmptyRule())
    val invalidResult: Invalid = validator.validate("") as Invalid
    assert(invalidResult.messageId == R.string.error_input_empty)
  }

  @Test
  fun validator_prohibitedSymbolsRule_OK() {
    val validator = InputValidator(ProhibitedSymbolsRule())
    assert(validator.validate("test") is OK)
  }

  @Test
  fun validator_prohibitedSymbolsRule_Invalid() {
    val validator = InputValidator(ProhibitedSymbolsRule("#".toRegex()))
    assert(validator.validate("test#") is Invalid)
  }

  @Test
  fun validator_prohibitedSymbolsRule_Invalid_correctMessage() {
    val validator = InputValidator(ProhibitedSymbolsRule("#".toRegex()))
    val invalidResult: Invalid = validator.validate("test#") as Invalid
    assert(invalidResult.messageId == R.string.error_input_incorrect_symbols)
  }

  @Test
  fun validator_listOfRules_OK() {
    val validator = InputValidator(NotEmptyRule(), ProhibitedSymbolsRule())
    assert(validator.validate("test") is OK)
  }

  @Test
  fun validator_listOfRules_Invalid() {
    val validator = InputValidator(NotEmptyRule(), ProhibitedSymbolsRule())
    assert(validator.validate("#") is Invalid)
  }

  @Test
  fun validator_listOfRules_Invalid_correctMessage() {
    val validator = InputValidator(NotEmptyRule(), ProhibitedSymbolsRule("#".toRegex()))
    val invalidResult: Invalid = validator.validate("test#") as Invalid
    assert(invalidResult.messageId == R.string.error_input_incorrect_symbols)
  }

  @Test
  fun validator_listOfRules_emptyFirst_correctMessage() {
    val validator = InputValidator(NotEmptyRule(), ProhibitedSymbolsRule("#".toRegex()))
    val invalidResult: Invalid = validator.validate("") as Invalid
    assert(invalidResult.messageId == R.string.error_input_empty)
  }

  @Test
  fun validator_listOfRules_prohibitedSymbolsFirst_correctMessage() {
    val validator = InputValidator(ProhibitedSymbolsRule("#".toRegex()), NotEmptyRule())
    val invalidResult: Invalid = validator.validate("test#") as Invalid
    assert(invalidResult.messageId == R.string.error_input_incorrect_symbols)
  }
}