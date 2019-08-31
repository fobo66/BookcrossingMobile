package com.bookcrossing.mobile.presenters;

import android.os.Build;
import com.bookcrossing.mobile.modules.TestApp;
import com.bookcrossing.mobile.ui.scan.ScanView$$State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * (c) 2019 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 2019-08-31.
 */
@Config(sdk = Build.VERSION_CODES.P, application = TestApp.class)
@RunWith(RobolectricTestRunner.class) public class ScanPresenterTest {

  private ScanPresenter presenter;

  @Mock ScanView$$State state;

  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
    presenter = new ScanPresenter();
    presenter.setViewState(state);
  }

  @Test public void testValidUrl() {
    presenter.checkBookcrossingUri("bookcrossing://com.bookcrossing.mobile/book?key=123");
    verify(state).onBookCodeScanned(any());
  }

  @Test public void testInvalidUrl() {
    presenter.checkBookcrossingUri("https://example.com");
    verify(state).onIncorrectCodeScanned();
  }
}