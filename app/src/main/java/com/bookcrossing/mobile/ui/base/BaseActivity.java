package com.bookcrossing.mobile.ui.base;

import android.annotation.SuppressLint;

import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpAppCompatActivity;

@SuppressLint("Registered")
public class BaseActivity extends MvpAppCompatActivity {

  protected CompositeDisposable subscriptions = new CompositeDisposable();

  @Override protected void onDestroy() {
    super.onDestroy();
    subscriptions.dispose();
  }
}
