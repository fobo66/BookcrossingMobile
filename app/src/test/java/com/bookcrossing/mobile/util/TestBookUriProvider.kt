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
import java.net.URI
import java.net.URLDecoder

class TestBookUriProvider : BookUriProvider {
  override fun provideBookUri(rawUri: String): BookUri {
    val uri = URI(rawUri)

    return BookUri(
      uri.authority, uri.scheme, uri.path, splitQuery(uri)?.get(EXTRA_KEY)
    )
  }

  override fun buildBookUri(bookCode: String): String = URI(
    "bookcrossing",
    PACKAGE_NAME,
    "/book",
    "$EXTRA_KEY=$bookCode",
    null
  )
    .toString()

  private fun splitQuery(uri: URI): Map<String, String?>? {

    return uri.query?.split("&")
      ?.map {
        val index = it.indexOf("=")
        val key = if (index > 0) it.substring(0, index) else it
        val value = if (index > 0 && it.length > index + 1) it.substring(index + 1) else null

        URLDecoder.decode(key, "UTF-8") to
          URLDecoder.decode(value, "UTF-8")
      }
      ?.toMap()
  }
}