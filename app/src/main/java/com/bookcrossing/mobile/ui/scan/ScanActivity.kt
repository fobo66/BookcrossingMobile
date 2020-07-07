/*
 *    Copyright 2020 Andrey Mukamolov
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
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import butterknife.BindView
import butterknife.ButterKnife
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.R.string
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.ScanPresenter
import com.bookcrossing.mobile.ui.base.BaseActivity
import com.bookcrossing.mobile.util.observe
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.PublishSubject
import moxy.ktx.moxyPresenter
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Provider
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Screen for scanning book code on the flyleaf
 * Created 11.06.17.
 */
class ScanActivity : BaseActivity(), ScanView {

  @Inject
  lateinit var presenterProvider: Provider<ScanPresenter>

  private val presenter: ScanPresenter by moxyPresenter { presenterProvider.get() }

  @BindView(R.id.qrContainer)
  lateinit var container: CoordinatorLayout

  @BindView(R.id.qrCodeView)
  lateinit var readerView: PreviewView

  private var preview: Preview? = null
  private var imageAnalyzer: ImageAnalysis? = null
  private var camera: Camera? = null

  private val incorrectCodeScannedSnackbar: Snackbar by lazy(mode = NONE) {
    Snackbar.make(container, string.incorrect_code_scanned_message, Snackbar.LENGTH_SHORT)
  }

  private val cameraViewSize: Size by lazy(mode = NONE) {
    Size(1280, 720)
  }

  private val retryPermissionAction: PublishSubject<Boolean> = PublishSubject.create()

  private val startSubscriptions = CompositeDisposable()

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

    startSubscriptions.add(
      RxPermissions(this).request(Manifest.permission.CAMERA)
        .doOnError { handleError(it) }
        .switchMapSingle {
          ProcessCameraProvider.getInstance(this).observe(ContextCompat.getMainExecutor(this))
        }
        .doOnNext { setupScannerView(it) }
        .retryWhen { it.zipWith(retryPermissionAction) }
        .subscribe {
          Snackbar.make(container, R.string.scan_activity_initial_message, Snackbar.LENGTH_SHORT)
            .show()
        }
    )

    startSubscriptions.add(
      presenter.onBarcodeScanned()
        .subscribe { presenter.checkBookcrossingUri(it) }
    )
  }

  override fun onStop() {
    super.onStop()
    startSubscriptions.clear()
  }

  override fun onBookCodeScanned(uri: String) {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
    ContextCompat.startActivity(this, intent, options)
  }

  override fun onIncorrectCodeScanned() {
    if (!incorrectCodeScannedSnackbar.isShown) {
      incorrectCodeScannedSnackbar.show()
    }
  }

  private fun setupScannerView(cameraProvider: ProcessCameraProvider) {

    // Preview
    preview = Preview.Builder()
      .setTargetResolution(cameraViewSize)
      .build()

    // Select back camera
    val cameraSelector =
      CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

    imageAnalyzer = ImageAnalysis.Builder()
      .setTargetResolution(cameraViewSize)
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build()
      .also {
        it.setAnalyzer(Executors.newCachedThreadPool(), presenter.bookCodeAnalyzer)
      }

    // Unbind use cases before rebinding
    cameraProvider.unbindAll()

    // Bind use cases to camera
    camera = cameraProvider.bindToLifecycle(
      this, cameraSelector, imageAnalyzer, preview
    )
    preview?.setSurfaceProvider(readerView.createSurfaceProvider())
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