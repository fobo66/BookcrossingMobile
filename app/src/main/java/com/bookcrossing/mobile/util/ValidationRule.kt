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

/**
 * Rule by which correctness of user's input is determined
 */
interface ValidationRule {
  /**
   * Check if input string conforms with conditions defined in this rule
   *
   * @param input user's input
   *
   * @return result which describes, is input OK or not and specifies corresponding error message
   */
  fun check(input: String): ValidationResult
}

/**
 * Rule for allowing only non-empty strings
 */
class NotEmptyRule : ValidationRule {
  override fun check(input: String): ValidationResult {
    return if (input.isNotBlank()) OK else Invalid(R.string.error_input_empty)
  }
}

/**
 * Rule that prohibits certain symbols
 */
class ProhibitedSymbolsRule(
  private val prohibitedSymbols: Regex = "[*#\\[\\]?]".toRegex()
) : ValidationRule {
  override fun check(input: String): ValidationResult {
    return if (!input.contains(prohibitedSymbols)) OK
    else Invalid(R.string.error_input_incorrect_symbols)
  }
}

/**
 * Rule for limiting string length that can be inputted
 */
class LengthRule(
  private val minLength: Int = 0,
  private val maxLength: Int
) : ValidationRule {
  override fun check(input: String): ValidationResult {
    return when {
      input.length <= minLength -> {
        Invalid(R.string.error_too_short)
      }
      input.length > maxLength -> {
        Invalid(R.string.error_too_long)
      }
      else -> {
        OK
      }
    }
  }
}