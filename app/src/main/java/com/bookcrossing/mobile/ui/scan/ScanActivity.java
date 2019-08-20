package com.bookcrossing.mobile.ui.scan;

import android.Manifest;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.ScanPresenter;
import com.bookcrossing.mobile.ui.base.BaseActivity;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 11.06.17.
 */

public class ScanActivity extends BaseActivity
    implements ScanView, QRCodeReaderView.OnQRCodeReadListener {

  @InjectPresenter public ScanPresenter presenter;

  @BindView(R.id.qrContainer) public ViewGroup container;

  private QRCodeReaderView readerView;
  private PointsOverlayView pointsOverlayView;

  private RxPermissions permissions;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);
    ButterKnife.bind(this);
    permissions = new RxPermissions(this);
    subscriptions.add(
            permissions.request(Manifest.permission.CAMERA).subscribe(granted -> {
                if (granted) {
                    setupScannerView();
          }
        }));
  }

  private void setupScannerView() {
    View scannerView = getLayoutInflater().inflate(R.layout.content_scan, container);
    readerView = scannerView.findViewById(R.id.qrCodeView);
    pointsOverlayView = scannerView.findViewById(R.id.points);
    readerView.setOnQRCodeReadListener(ScanActivity.this);
    readerView.startCamera();
  }

  @Override protected void onResume() {
    super.onResume();
    Toast.makeText(ScanActivity.this, R.string.scan_activity_initial_message, Toast.LENGTH_SHORT)
        .show();
  }

  @Override protected void onPause() {
    super.onPause();
    readerView.stopCamera();
  }

  @Override public void onBookCodeScanned(Uri uri) {
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    startActivity(intent);
  }

  @Override public void onIncorrectCodeScanned() {
    Toast.makeText(this, R.string.incorrect_code_scanned_message, Toast.LENGTH_SHORT).show();
  }

  @Override public void onQRCodeRead(String text, PointF[] points) {
    pointsOverlayView.setPoints(points);
    presenter.checkBookcrossingUri(text);
  }
}
