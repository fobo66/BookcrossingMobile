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

package com.bookcrossing.mobile.presenters;

import android.text.TextUtils;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.BookCode;
import com.bookcrossing.mobile.ui.acquire.BookAcquireView;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Completable;
import io.reactivex.Maybe;

@InjectViewState public class BookAcquirePresenter extends BasePresenter<BookAcquireView> {

  public void handleAcquisitionResult(final BookCode code) {
    if (code instanceof BookCode.CorrectCode) {
      getViewState().onAcquired();
    } else if (code instanceof BookCode.IncorrectCode) {
      getViewState().onIncorrectKey();
    }
  }

  public Completable processBookAcquisition(String key) {
    return RxFirebaseDatabase.setValue(books().child(key).child("free"), Boolean.FALSE)
        .andThen(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class))
        .flatMapCompletable(
            book -> RxFirebaseDatabase.setValue(acquiredBooks().child(key), book));
  }

  public Maybe<BookCode> validateCode(final String key) {
    return RxFirebaseDatabase.observeSingleValueEvent(books())
        .flatMap(dataSnapshot -> {
          if (!TextUtils.isEmpty(key) && dataSnapshot.hasChild(key)) {
            return Maybe.just(new BookCode.CorrectCode(key));
          }

          return Maybe.just(BookCode.IncorrectCode.INSTANCE);
        });
  }
}
