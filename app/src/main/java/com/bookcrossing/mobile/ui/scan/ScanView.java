package com.bookcrossing.mobile.ui.scan;

import android.net.Uri;

import com.arellomobile.mvp.MvpView;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 11.06.17.
 */

public interface ScanView extends MvpView {
    void onBookCodeScanned(Uri uri);
    void onIncorrectCodeScanned();
}
