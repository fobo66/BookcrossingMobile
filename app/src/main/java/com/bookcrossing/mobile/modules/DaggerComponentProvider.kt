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

package com.bookcrossing.mobile.modules

import android.app.Activity
import androidx.fragment.app.Fragment
import com.bookcrossing.mobile.components.AppComponent

/**
 * Convenience interface for neat access to dependency tree
 */
interface DaggerComponentProvider {

  val component: AppComponent
}

/**
 * Convenience accessor to dependency tree
 */
val Activity.injector get() = (application as DaggerComponentProvider).component

/**
 * Convenience accessor to dependency tree
 */
val Fragment.injector get() = (requireActivity().application as DaggerComponentProvider).component