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

package com.bookcrossing.mobile.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bookcrossing.mobile.util.BookListenerDelegate;
import com.bookcrossing.mobile.util.ConstantsKt;
import com.firebase.ui.auth.AuthUI;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Collections;
import moxy.MvpAppCompatFragment;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 16.11.2016.
 */

public abstract class BaseFragment extends MvpAppCompatFragment {

  protected Unbinder unbinder;
  protected CompositeDisposable subscriptions = new CompositeDisposable();
  protected BookListenerDelegate listener;

  @Override public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    listener = new BookListenerDelegate(context);
  }

  @Override public void onDetach() {
    super.onDetach();
    listener.detachListener();
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);
  }

  protected void authenticate() {
    startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build()))
      .build(), ConstantsKt.RC_SIGN_IN);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    subscriptions.dispose();
  }
}
