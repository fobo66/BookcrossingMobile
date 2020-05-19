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
package com.bookcrossing.mobile.models

/**
 * Container class to process book release process
 */
class BookBuilder {
  private var author: String? = null
  private var name: String? = null
  private var description: String? = null
  private var free = true
  private var position: Coordinates? = null
  private var city: String? = null
  private var positionName: String? = null
  private var wentFreeAt: Date? = null
  fun setAuthor(author: String): BookBuilder {
    this.author = author
    return this
  }

  fun setName(name: String): BookBuilder {
    this.name = name
    return this
  }

  fun setDescription(description: String): BookBuilder {
    this.description = description
    return this
  }

  fun setFree(free: Boolean): BookBuilder {
    this.free = free
    return this
  }

  fun setPositionName(positionName: String): BookBuilder {
    this.positionName = positionName
    return this
  }

  fun setWentFreeAt(wentFreeAt: Date): BookBuilder {
    this.wentFreeAt = wentFreeAt
    return this
  }

  fun setPosition(coordinates: Coordinates): BookBuilder {
    position = coordinates
    return this
  }

  fun setCity(city: String): BookBuilder {
    this.city = city
    return this
  }

  fun createBook(): Book {
    return Book(
      author,
      name,
      description,
      free,
      positionName,
      position,
      city,
      wentFreeAt
    )
  }

  fun clear() {
    author = null
    name = null
    description = null
    free = true
    position = null
    positionName = null
    city = null
    wentFreeAt = null
  }
}