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

package com.bookcrossing.mobile.presenters;

import androidx.annotation.NonNull;

import com.bookcrossing.mobile.ui.bookpreview.BookItemView;

import moxy.InjectViewState;

@InjectViewState public class BookItemPresenter extends BasePresenter<BookItemView> {

  public static final String TAG = "BookItemPresenter";

  public void releaseCurrentBook(@NonNull String key, @NonNull String position) {
    acquiredBooks().child(key).removeValue();
    books().child(key).child("city").setValue(getCity());
    books().child(key).child("positionName").setValue(position);
    books().child(key).child("free").setValue(true);
  }
}
