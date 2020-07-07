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

import android.net.Uri
import androidx.core.net.toUri
import com.bookcrossing.mobile.models.BookUri

class BookUriProviderImpl : BookUriProvider {
  override fun provideBookUri(rawUri: String): BookUri {
    val uri = rawUri.toUri()

    return BookUri(
      uri.authority, uri.scheme, uri.path, uri.getQueryParameter(EXTRA_KEY)
    )
  }

  override fun buildBookUri(bookCode: String): String = Uri.Builder()
    .scheme("bookcrossing")
    .authority(PACKAGE_NAME)
    .path("book")
    .appendQueryParameter(EXTRA_KEY, bookCode)
    .build()
    .toString()
}