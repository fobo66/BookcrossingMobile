/*
 *    Copyright 2019 Andrey Mukamolov
 *
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

package com.bookcrossing.mobile.ui.scan

import android.Manifest
import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.presenters.ScanPresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.github.florent37.runtimepermission.rx.RxPermissions
import moxy.presenter.InjectPresenter

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 11.06.17.
 */

class ScanActivity : BaseActivity(), ScanView, QRCodeReaderView.OnQRCodeReadListener {

  @InjectPresenter
  lateinit var presenter: ScanPresenter

  @BindView(R.id.qrContainer)
  lateinit var container: ViewGroup

  private lateinit var readerView: QRCodeReaderView
  private lateinit var pointsOverlayView: PointsOverlayView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scan)
    ButterKnife.bind(this)
    subscriptions.add(
      RxPermissions(this).request(Manifest.permission.CAMERA).subscribe { granted -> setupScannerView() })
  }

  private fun setupScannerView() {
    val scannerView = layoutInflater.inflate(R.layout.content_scan, container)
    readerView = scannerView.findViewById(R.id.qrCodeView)
    pointsOverlayView = scannerView.findViewById(R.id.points)
    readerView.setOnQRCodeReadListener(this@ScanActivity)
    readerView.startCamera()
  }

  override fun onResume() {
    super.onResume()
    Toast.makeText(this, R.string.scan_activity_initial_message, Toast.LENGTH_SHORT).show()
  }

  override fun onPause() {
    super.onPause()
    readerView.stopCamera()
  }

  override fun onBookCodeScanned(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
  }

  override fun onIncorrectCodeScanned() {
    Toast.makeText(this, R.string.incorrect_code_scanned_message, Toast.LENGTH_SHORT).show()
  }

  override fun onQRCodeRead(text: String, points: Array<PointF>) {
    pointsOverlayView.setPoints(points)
    presenter.checkBookcrossingUri(text)
  }
}
