/*
 *     Copyright 2019 Andrey Mukamolov
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.bookcrossing.mobile.ui.create

import android.Manifest
import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.BindString
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.presenters.BookCreatePresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.DEFAULT_DEBOUNCE_TIMEOUT
import com.bookcrossing.mobile.util.PROHIBITED_SYMBOLS
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import com.miguelbcr.ui.rx_paparazzo2.entities.Response
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import moxy.presenter.InjectPresenter
import java.util.concurrent.TimeUnit

class BookCreateFragment : BaseFragment(), BookCreateView {

  @InjectPresenter lateinit var presenter: BookCreatePresenter

  @BindView(R.id.cover) lateinit var cover: ImageView

  @BindView(R.id.input_name) lateinit var bookNameInput: TextView

  @BindView(R.id.input_author) lateinit var bookAuthorInput: TextView

  @BindView(R.id.input_position) lateinit var bookPositionInput: TextView

  @BindView(R.id.input_description) lateinit var bookDescriptionInput: TextView

  @BindView(R.id.publish_book)
  lateinit var releaseButton: Button

  @BindString(R.string.rendered_sticker_name) lateinit var stickerName: String

  @BindString(R.string.rendered_sticker_description) lateinit var stickerDescription: String

  private var coverChooserDialog: MaterialDialog? = null

  private lateinit var permissions: RxPermissions

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_book_create, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    permissions = RxPermissions(this)

    buildCoverChooserDialog()
    registerSubscriptions()
  }

  private fun buildCoverChooserDialog() {
    coverChooserDialog = MaterialDialog(requireContext())
      .title(R.string.cover_chooser_title, null)
      .listItems(R.array.cover_chooser_dialog_items, selection = { _, index, _ ->
        val chooserObservable: Observable<Response<BookCreateFragment, FileData>> =
          if (index == 0) {
            requestCoverImageFromGallery()
          } else {
            requestCoverImageFromCamera()
          }
        subscriptions.add(chooserObservable.subscribe { result ->
          if (result.resultCode() == RESULT_OK) {
            result.targetUI()
              .presenter.saveCoverTemporarily(result.data())
          }
        })
      })
  }

  private fun registerSubscriptions() {
    registerBookSubscriptions()
    registerPublishButtonEnableSubscription()
    registerPublishButtonClickSubscription()
  }

  private fun registerBookSubscriptions() {
    registerCoverClickSubscription()
    registerNameInputSubscription()
    registerAuthorInputSubscription()
    registerPositionInputSubscription()
    registerDescriptionInputSubscription()
  }

  private fun registerPublishButtonClickSubscription() {
    val publishSubscription = releaseButton.clicks()
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { releaseButton.isEnabled = false }
      .switchMap { presenter.publishBook() }
      .subscribe()
    subscriptions.add(publishSubscription)
  }

  private fun registerPublishButtonEnableSubscription() {
    val publishSubscription =
      Observables.zip(
        bookNameInput.textChanges(),
        bookAuthorInput.textChanges(),
        bookPositionInput.textChanges(),
        bookDescriptionInput.textChanges()
      ) { name: CharSequence, author: CharSequence, position: CharSequence, description: CharSequence ->
        name.isNotBlank() &&
          author.isNotBlank() &&
          position.isNotBlank() &&
          description.isNotBlank()
      }
        .subscribe { enabled: Boolean -> releaseButton.isEnabled = enabled }
    subscriptions.add(publishSubscription)
  }

  private fun registerDescriptionInputSubscription() {
    val descriptionSubscription = bookDescriptionInput.afterTextChangeEvents()
      .throttleLast(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .filter { event -> !event.view.text.toString().contains(PROHIBITED_SYMBOLS) }
      .subscribe { event -> presenter.onDescriptionChange(event.view.text.toString()) }
    subscriptions.add(descriptionSubscription)
  }

  private fun registerPositionInputSubscription() {
    val positionSubscription = bookPositionInput.afterTextChangeEvents()
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .filter { event -> !event.view.text.toString().contains(PROHIBITED_SYMBOLS) }
      .subscribe { event -> presenter.onPositionChange(event.view.text.toString()) }
    subscriptions.add(positionSubscription)
  }

  private fun registerAuthorInputSubscription() {
    val authorSubscription = bookAuthorInput.afterTextChangeEvents()
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .filter { event -> !event.view.text.toString().contains(PROHIBITED_SYMBOLS) }
      .subscribe { event -> presenter.onAuthorChange(event.view.text.toString()) }
    subscriptions.add(authorSubscription)
  }

  private fun registerNameInputSubscription() {
    val nameSubscription = bookNameInput.afterTextChangeEvents()
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .filter { event ->
        val textFieldValue = event.view.text
          .toString()
        !textFieldValue.contains(PROHIBITED_SYMBOLS) && textFieldValue.isNotEmpty()
      }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { event -> presenter.onNameChange(event.view.text.toString()) }
    subscriptions.add(nameSubscription)
  }

  private fun registerCoverClickSubscription() {
    val coverSubscription = cover.clicks()
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .subscribe { coverChooserDialog!!.show() }
    subscriptions.add(coverSubscription)
  }

  private fun requestCoverImageFromGallery(): Observable<Response<BookCreateFragment, FileData>> {
    return RxPaparazzo.single(this)
      .usingGallery()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  private fun requestCoverImageFromCamera(): Observable<Response<BookCreateFragment, FileData>> {
    return RxPaparazzo.single(this)
      .usingCamera()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  override fun onCoverChosen(coverUri: Uri?) {
    GlideApp.with(this)
      .load(coverUri)
      .transition(withCrossFade())
      .into(cover)
  }

  override fun showCover() {
    if (cover.visibility == View.GONE) {
      cover.visibility = View.VISIBLE
    }
  }

  override fun onReleased(newKey: String) {
    val dialog = MaterialDialog(requireContext())
      .title(R.string.book_saved_dialog_title)
      .customView(R.layout.book_sticker_layout)
      .positiveButton(R.string.ok) { dialog ->
        renderSticker(dialog.getCustomView().findViewById(R.id.sticker))
        requireActivity().supportFragmentManager.popBackStack()
        listener.onBookReleased(newKey)
      }

    prepareDialog(dialog.getCustomView().findViewById(R.id.sticker), newKey)
    dialog.show()
  }

  override fun onFailedToRelease() {
    MaterialDialog(requireContext())
      .message(R.string.failed_to_release_book_message, null, null)
      .title(R.string.error_dialog_title, null)
      .positiveButton(R.string.ok, null) { dialog ->
        dialog.dismiss()
      }
      .show()
  }

  private fun prepareDialog(
    stickerView: View,
    key: String
  ) {
    val qrCode = stickerView.findViewById<ImageView>(R.id.qr_code)
    val keyView = stickerView.findViewById<TextView>(R.id.sticker_book_key)
    qrCode.setImageBitmap(presenter.generateQrCode(key))
    keyView.text = key
  }

  private fun renderSticker(sticker: View) {
    sticker.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
    val stickerBitmap = Bitmap.createBitmap(sticker.width, sticker.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(stickerBitmap)
    sticker.draw(canvas)
    presenter.saveSticker(stickerBitmap, stickerName, stickerDescription)
  }

  private fun resolveCity() {
    subscriptions.add(
      permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION)
        .filter { granted -> granted }
        .flatMapMaybe { presenter.resolveUserCity() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { city ->
          if (city.isNotEmpty()) {
            presenter.saveCity(city)
          } else {
            askUserToProvideDefaultCity()
          }
        })
  }

  private fun askUserToProvideDefaultCity() {
    MaterialDialog(requireContext())
      .title(R.string.enter_city_title)
      .message(R.string.error_enter_city_content)
      .input(hintRes = R.string.city_hint, callback =
      { _, input -> presenter.saveCity(input.toString()) })
      .show()
  }
}
