package com.bookcrossing.mobile.ui.acquire;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.BookAcquirePresenter;
import com.bookcrossing.mobile.util.Constants;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class BookAcquireActivity extends MvpAppCompatActivity implements BookAcquireView {

    @InjectPresenter
    BookAcquirePresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.submit)
    Button submitButton;

    @BindView(R.id.input_code)
    TextInputEditText codeInput;

    @BindView(R.id.coord_layout)
    CoordinatorLayout coordinatorLayout;

    private String keyToAcquire;
    private boolean isInnerAppRequest;

    private Disposable acquisitionDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_acquire);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if (getIntent() != null) {
            keyToAcquire = getIntent().getData().getQueryParameter(Constants.EXTRA_KEY);
            isInnerAppRequest = getIntent().getBooleanExtra(getString(R.string.extra_insideAppRequest), false);
            if (!isInnerAppRequest) {
                codeInput.setText(keyToAcquire);
                codeInput.setEnabled(false);
            }
        }

        acquisitionDisposable = RxView.clicks(submitButton)
                .filter(new Predicate<Object>() {
                    @Override
                    public boolean test(@NonNull Object o) throws Exception {
                        return presenter.isKeyValid(codeInput.getText().toString());
                    }
                })
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        presenter.handleAcquisition(codeInput.getText().toString());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        acquisitionDisposable.dispose();
    }

    @Override
    public void onIncorrectKey() {
        Snackbar.make(coordinatorLayout, R.string.incorrect_key_message, Snackbar.LENGTH_SHORT).show();
    }
}
