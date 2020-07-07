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

package com.bookcrossing.mobile.modules

import com.bookcrossing.mobile.models.BookUri
import com.bookcrossing.mobile.util.BookUriAuthorityRule
import com.bookcrossing.mobile.util.BookUriCodeRule
import com.bookcrossing.mobile.util.BookUriPathRule
import com.bookcrossing.mobile.util.BookUriSchemeRule
import com.bookcrossing.mobile.util.LengthRule
import com.bookcrossing.mobile.util.NotEmptyRule
import com.bookcrossing.mobile.util.Validator
import dagger.Module
import dagger.Provides

@Module
class ValidatorsModule {

  @Provides
  @InputValidator
  fun provideInputValidator(): Validator<String> =
    Validator(NotEmptyRule(), LengthRule(maxLength = 100))

  @Provides
  @BookUriValidator
  fun provideBookCodeValidator(): Validator<BookUri> = Validator(
    BookUriAuthorityRule(),
    BookUriSchemeRule(),
    BookUriPathRule(),
    BookUriCodeRule()
  )
}