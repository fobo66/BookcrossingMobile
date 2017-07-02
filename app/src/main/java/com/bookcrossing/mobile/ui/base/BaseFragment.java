package com.bookcrossing.mobile.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.arellomobile.mvp.MvpAppCompatFragment;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.listeners.BookListener;
import com.firebase.ui.auth.AuthUI;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Collections;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 16.11.2016.
 */

public abstract class BaseFragment extends MvpAppCompatFragment {

  protected Unbinder unbinder;
  protected CompositeDisposable subscriptions = new CompositeDisposable();
  protected BookListener listener;

  public abstract @StringRes int title();

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof BookListener) {
      listener = (BookListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement BookListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    listener = null;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
            Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
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
