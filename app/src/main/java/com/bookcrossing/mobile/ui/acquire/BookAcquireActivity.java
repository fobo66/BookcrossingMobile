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

package com.bookcrossing.mobile.ui.acquire;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.BookCode;
import com.bookcrossing.mobile.presenters.BookAcquirePresenter;
import com.bookcrossing.mobile.ui.bookpreview.BookActivity;
import com.bookcrossing.mobile.ui.scan.ScanActivity;
import com.bookcrossing.mobile.util.ConstantsKt;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding3.view.RxView;
import com.jakewharton.rxbinding3.widget.RxTextView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import kotlin.Unit;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import org.jetbrains.annotations.NotNull;

public class BookAcquireActivity extends MvpAppCompatActivity implements BookAcquireView {

  @InjectPresenter public BookAcquirePresenter presenter;

  @BindView(R.id.toolbar) public Toolbar toolbar;

  @BindView(R.id.acquireButton) public Button acquireButton;

  @BindView(R.id.scan_code) public Button scanCodeButton;

  @BindView(R.id.input_code) public TextInputEditText codeInput;

  @BindView(R.id.coord_layout) public CoordinatorLayout coordinatorLayout;

  private String keyToAcquire;
  private boolean isInnerAppRequest;

  private Disposable acquisitionDisposable;
  private Disposable scanDisposable;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_acquire);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    if (getIntent() != null) {
      keyToAcquire = getIntent().getData().getQueryParameter(ConstantsKt.EXTRA_KEY);
      isInnerAppRequest =
          getIntent().getBooleanExtra(getString(R.string.extra_insideAppRequest), false);
      if (!isInnerAppRequest) {
        codeInput.setText(keyToAcquire);
        codeInput.setEnabled(false);
      }
    }

    acquisitionDisposable = onAcquireButtonClicked()
        .withLatestFrom(onCodeValueChanged(), (o, code) -> code.toString())
        .flatMapMaybe(code -> presenter.validateCode(code))
        .flatMap(code -> {
          if (code instanceof BookCode.CorrectCode) {
            return presenter.processBookAcquisition(((BookCode.CorrectCode) code).getCode())
                .andThen(Observable.just(code));
          }

          return Observable.just(code);
        })
        .subscribe(code -> presenter.handleAcquisitionResult(code));

    scanDisposable = RxView.clicks(scanCodeButton)
        .throttleFirst(300, TimeUnit.MILLISECONDS)
        .subscribe(o -> startActivity(new Intent(BookAcquireActivity.this, ScanActivity.class)));
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    acquisitionDisposable.dispose();
    scanDisposable.dispose();
  }

  @Override public void onIncorrectKey() {
    Snackbar.make(coordinatorLayout, R.string.incorrect_key_message, Snackbar.LENGTH_SHORT).show();
  }

  @Override public void onAcquired() {
    Intent intent = new Intent(this, BookActivity.class);
    intent.putExtra(ConstantsKt.EXTRA_KEY, keyToAcquire);
    startActivity(intent);
  }

  @NotNull private Observable<Unit> onAcquireButtonClicked() {
    return RxView.clicks(acquireButton)
        .throttleFirst(300, TimeUnit.MILLISECONDS);
  }

  @NotNull private Observable<CharSequence> onCodeValueChanged() {
    return RxTextView.textChanges(codeInput);
  }
}
