package com.bookcrossing.mobile.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.arellomobile.mvp.MvpAppCompatFragment;
import com.bookcrossing.mobile.util.BookListenerDelegate;
import com.bookcrossing.mobile.util.Constants;
import com.firebase.ui.auth.AuthUI;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Collections;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 16.11.2016.
 */

public abstract class BaseFragment extends MvpAppCompatFragment {

  protected Unbinder unbinder;
  protected CompositeDisposable subscriptions = new CompositeDisposable();
  protected BookListenerDelegate listener;

  public abstract @StringRes int title();

  @Override public void onAttach(Context context) {
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
    setActivityTitle();
  }

  private void setActivityTitle() {
    listener.setTitle(title());
  }

  protected void authenticate() {
    startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build()))
        .build(), Constants.RC_SIGN_IN);
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
