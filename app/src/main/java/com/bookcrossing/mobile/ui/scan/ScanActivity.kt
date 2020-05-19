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
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.ScanPresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.PublishSubject
import moxy.ktx.moxyPresenter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 11.06.17.
 */

class ScanActivity : BaseActivity(), ScanView, QRCodeReaderView.OnQRCodeReadListener {

  @Inject
  lateinit var presenterProvider: Provider<ScanPresenter>

  private val presenter: ScanPresenter by moxyPresenter { presenterProvider.get() }

  @BindView(R.id.qrContainer)
  lateinit var container: CoordinatorLayout

  private var readerView: PreviewView? = null
  private var pointsOverlayView: PointsOverlayView? = null

  private var preview: Preview? = null
  private var imageCapture: ImageCapture? = null
  private var imageAnalyzer: ImageAnalysis? = null
  private var camera: Camera? = null


  private val retryPermissionAction: PublishSubject<Boolean> = PublishSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    injector.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scan)
    ButterKnife.bind(this)

    container.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
  }

  override fun onStart() {
    super.onStart()

    subscriptions.add(
      RxPermissions(this).request(Manifest.permission.CAMERA)
        .doOnError { handleError(it) }
        .retryWhen { it.zipWith(retryPermissionAction) }
        .subscribe { setupScannerView() }
    )
  }


  override fun onBookCodeScanned(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
  }

  override fun onIncorrectCodeScanned() {
    Snackbar.make(container, R.string.incorrect_code_scanned_message, Snackbar.LENGTH_SHORT).show()
  }

  override fun onQRCodeRead(text: String, points: Array<PointF>) {
    pointsOverlayView?.setPoints(points)
    presenter.checkBookcrossingUri(text)
  }

  private fun setupScannerView() {
    val scannerView = layoutInflater.inflate(R.layout.content_scan, container)
    readerView = scannerView.findViewById(R.id.qrCodeView)
    pointsOverlayView = scannerView.findViewById(R.id.points)

    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

    cameraProviderFuture.addListener(Runnable {
      // Used to bind the lifecycle of cameras to the lifecycle owner
      val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

      // Preview
      preview = Preview.Builder()
        .build()

      // Select back camera
      val cameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

      try {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        camera = cameraProvider.bindToLifecycle(
          this, cameraSelector, preview
        )
        preview?.setSurfaceProvider(readerView?.createSurfaceProvider(camera?.cameraInfo))
        Snackbar.make(container, R.string.scan_activity_initial_message, Snackbar.LENGTH_SHORT)
          .show()
      } catch (exc: Exception) {
        Timber.e(exc, "Use case binding failed")
      }
    }, ContextCompat.getMainExecutor(this))

  }

  private fun handleError(throwable: Throwable) {
    val result = (throwable as RxPermissions.Error).result

    val snackbar =
      Snackbar.make(container, R.string.camera_permission_denied_prompt, Snackbar.LENGTH_SHORT)

    if (result.hasDenied()) {
      snackbar
        .setAction(R.string.retry_permission_request_action) { retryPermissionAction.onNext(true) }
    }

    if (result.hasForeverDenied()) {
      snackbar
        .setAction(R.string.go_to_settings_permission_request_action) { result.goToSettings() }
    }

    snackbar.show()
  }
}