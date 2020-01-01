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

package com.bookcrossing.mobile.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BookBuilder {
  private @Nullable String author;
  private @Nullable String name;
  private @Nullable String description;
  private boolean free = true;
  private @Nullable Coordinates position;
  private @Nullable String city;
  private @Nullable String positionName;
  private @Nullable Date wentFreeAt;

  @NonNull public BookBuilder setAuthor(@NonNull String author) {
    this.author = author;
    return this;
  }

  @NonNull public BookBuilder setName(@NonNull String name) {
    this.name = name;
    return this;
  }

  @NonNull public BookBuilder setDescription(@NonNull String description) {
    this.description = description;
    return this;
  }

  @NonNull public BookBuilder setFree(boolean free) {
    this.free = free;
    return this;
  }

  @NonNull public BookBuilder setPositionName(@NonNull String positionName) {
    this.positionName = positionName;
    return this;
  }

  @NonNull public BookBuilder setWentFreeAt(@NonNull Date wentFreeAt) {
    this.wentFreeAt = wentFreeAt;
    return this;
  }

  @NonNull public BookBuilder setPosition(@NonNull Coordinates coordinates) {
    this.position = coordinates;
    return this;
  }

  @NonNull public BookBuilder setCity(@NonNull String city) {
    this.city = city;
    return this;
  }

  @NonNull public Book createBook() {
    return new Book(author, name, description, free, position, positionName, city, wentFreeAt);
  }

  public void clear() {
    author = null;
    name = null;
    description = null;
    free = true;
    position = null;
    positionName = null;
    city = null;
    wentFreeAt = null;
  }
}