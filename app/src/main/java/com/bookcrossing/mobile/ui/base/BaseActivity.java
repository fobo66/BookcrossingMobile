package com.bookcrossing.mobile.ui.base;

import com.arellomobile.mvp.MvpAppCompatActivity;
import io.reactivex.disposables.CompositeDisposable;

public class BaseActivity extends MvpAppCompatActivity {

  protected CompositeDisposable subscriptions = new CompositeDisposable();

  @Override protected void onDestroy() {
    super.onDestroy();
    subscriptions.dispose();
  }
}
