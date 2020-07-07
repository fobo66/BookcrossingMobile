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

import com.bookcrossing.mobile.models.BookUri
import com.bookcrossing.mobile.modules.BookUriValidator
import com.bookcrossing.mobile.util.BookUriProvider
import com.bookcrossing.mobile.util.ValidationResult
import com.bookcrossing.mobile.util.Validator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCodeInteractor @Inject constructor(
  private val bookUriProvider: BookUriProvider,
  @BookUriValidator private val validator: Validator<BookUri>
) {

  fun checkBookcrossingUri(bookUri: String): ValidationResult {
    val uri = bookUriProvider.provideBookUri(bookUri)

    return validator.validate(uri)
  }

  fun buildBookUri(bookKey: String): String = bookUriProvider.buildBookUri(bookKey)
}