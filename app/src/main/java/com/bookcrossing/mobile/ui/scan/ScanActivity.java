package com.bookcrossing.mobile.ui.scan;

import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.ScanPresenter;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * (c) 2017 Andrey Mukamolow <fobo66@protonmail.com>
 * Created 11.06.17.
 */

public class ScanActivity extends MvpAppCompatActivity implements ScanView, QRCodeReaderView.OnQRCodeReadListener {

    @InjectPresenter
    ScanPresenter presenter;

    @BindView(R.id.qrdecoderview)
    QRCodeReaderView readerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        readerView.setOnQRCodeReadListener(this);
        Toast.makeText(this, R.string.scan_activity_initial_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        readerView.stopCamera();
    }

    @Override
    public void onBookCodeScanned(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onIncorrectCodeScanned() {
        Toast.makeText(this, R.string.incorrect_code_scanned_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        presenter.checkBookcrossingUri(text);
    }
}
