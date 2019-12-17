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

import com.bookcrossing.mobile.util.ValidationResult.OK

/**
 * Validate string input from the user by list of predefined rules
 */
class InputValidator(
  private val rules: List<ValidationRule>
) {

  constructor(vararg rulesArray: ValidationRule) : this(rulesArray.asList())

  /**
   * Validate string input
   *
   * @param input User's input from text field
   */
  fun validate(input: String): ValidationResult {
    val initial: ValidationResult = OK
    return rules.fold(initial) { acc: ValidationResult, validationRule: ValidationRule ->
      if (acc is OK) {
        validationRule.check(input)
      } else {
        acc
      }
    }
  }
}
