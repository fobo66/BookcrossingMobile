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

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.android.gms.tasks.Task
import io.reactivex.Single

/** Wrap Play Services Task API with RxJava */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
fun <T> Task<T>.observe(): Single<T> = Single.create { emitter ->
  addOnCompleteListener {
    if (it.isSuccessful) {
      if (!emitter.isDisposed) {
        it.result?.let { result -> emitter.onSuccess(result) } ?: emitter.onError(
          TaskException("Task result was null")
        )
      }
    } else {
      if (!emitter.isDisposed) {
        emitter.onError(
          TaskFailedException(
            "Task result retrieving was unsuccessful",
            it.exception
          )
        )
      }
    }
  }
}

/** Indicates that Task result is null */
class TaskException(message: String) : Exception(message)

/** Indicates that task completion was unsuccessful */
class TaskFailedException(message: String, cause: Throwable?) : Exception(message, cause)