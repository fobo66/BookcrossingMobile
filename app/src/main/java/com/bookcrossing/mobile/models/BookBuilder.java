/*
 *    Copyright  2019 Andrey Mukamolov
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

import org.jetbrains.annotations.NotNull;

public class BookBuilder {
  private String author;
  private String name;
  private String description;
  private boolean free = true;
  private Coordinates position;
  private String positionName;
  private Date wentFreeAt;

  public BookBuilder setAuthor(String author) {
    this.author = author;
    return this;
  }

  public BookBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public BookBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public BookBuilder setFree(boolean free) {
    this.free = free;
    return this;
  }

  public BookBuilder setPositionName(String positionName) {
    this.positionName = positionName;
    return this;
  }

  public BookBuilder setWentFreeAt(Date wentFreeAt) {
    this.wentFreeAt = wentFreeAt;
    return this;
  }

  public Book createBook() {
    return new Book(author, name, description, free, position, positionName, wentFreeAt);
  }

  public BookBuilder setPosition(@NotNull Coordinates coordinates) {
    this.position = coordinates;
    return this;
  }
}