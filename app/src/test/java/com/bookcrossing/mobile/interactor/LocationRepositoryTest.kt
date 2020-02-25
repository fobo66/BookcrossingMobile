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

package com.bookcrossing.mobile.interactor

import io.reactivex.Single
import org.junit.Test

/** Test hypotheses behind LocationRepository */
class LocationRepositoryTest {

  @Test
  fun testMaybeNotEmptyList() {
    Single.just(listOf(1, 2, 3))
      .filter { it.isNotEmpty() }
      .map { it[0] }
      .test()
      .assertResult(1)
  }

  @Test
  fun testMaybeEmptyList() {
    Single.just(emptyList<Int>())
      .filter { it.isNotEmpty() }
      .map { it[0] }
      .switchIfEmpty(Single.just(1))
      .test()
      .assertResult(1)
  }
}