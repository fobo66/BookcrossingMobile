package com.bookcrossing.mobile.ui.base;

import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;

public class BaseActivity extends AppCompatActivity {

    protected CompositeDisposable subscriptions = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.dispose();
    }
}
