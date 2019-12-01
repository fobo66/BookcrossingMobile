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

package com.bookcrossing.mobile.ui.create

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import butterknife.BindString
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.bookcrossing.mobile.BuildConfig
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.presenters.BookCreatePresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.util.DEFAULT_DEBOUNCE_TIMEOUT
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import moxy.presenter.InjectPresenter
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class BookCreateFragment : BaseFragment(), BookCreateView {

  @InjectPresenter
  lateinit var presenter: BookCreatePresenter

  @BindView(R.id.cover)
  lateinit var cover: ImageView

  @BindView(R.id.input_name)
  lateinit var bookNameInput: TextView

  @BindView(R.id.input_author)
  lateinit var bookAuthorInput: TextView

  @BindView(R.id.input_position)
  lateinit var bookPositionInput: TextView

  @BindView(R.id.input_description)
  lateinit var bookDescriptionInput: TextView

  @BindView(R.id.publish_book)
  lateinit var releaseButton: Button

  @BindString(R.string.rendered_sticker_name)
  lateinit var stickerName: String

  @BindString(R.string.rendered_sticker_description)
  lateinit var stickerDescription: String

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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
      presenter.saveCoverTemporarily(data?.data)
    }

    if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
      presenter.compressCoverPhoto(requireContext().contentResolver)
    }
  }

  private fun buildCoverChooserDialog() {
    coverChooserDialog = MaterialDialog(requireContext())
      .title(R.string.cover_chooser_title)
      .listItems(R.array.cover_chooser_dialog_items, selection = { _, index, _ ->
        if (index == 0) {
          requestCoverImageFromGallery()
        } else {
          requestCoverImageFromCamera()
        }
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
      .flatMap { resolveUserCity() }
      .flatMap { presenter.releaseBook(it) }
      .retry()
      .subscribe()
    subscriptions.add(publishSubscription)
  }

  private fun registerPublishButtonEnableSubscription() {
    val publishSubscription =
      Observables.combineLatest(
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
        .throttleLast(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { enabled: Boolean -> releaseButton.isEnabled = enabled }
    subscriptions.add(publishSubscription)
  }

  private fun registerDescriptionInputSubscription() {
    val descriptionSubscription = bookDescriptionInput.afterTextChangeEvents()
      .doOnNext {
        clearTextViewError(bookDescriptionInput)
      }
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .subscribe { event -> presenter.onDescriptionChange(event.view.text.toString()) }
    subscriptions.add(descriptionSubscription)
  }

  override fun onDescriptionError() {
    bookDescriptionInput.error = getString(R.string.error_book_description_malformed)
  }

  private fun registerPositionInputSubscription() {
    val positionSubscription = bookPositionInput.afterTextChangeEvents()
      .doOnNext {
        clearTextViewError(bookPositionInput)
      }
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .subscribe { event -> presenter.onPositionChange(event.view.text.toString()) }
    subscriptions.add(positionSubscription)
  }

  override fun onPositionError() {
    bookPositionInput.error = getString(R.string.error_book_position_malformed)
  }

  private fun registerAuthorInputSubscription() {
    val authorSubscription = bookAuthorInput.afterTextChangeEvents()
      .doOnNext {
        clearTextViewError(bookAuthorInput)
      }
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .subscribe { event -> presenter.onAuthorChange(event.view.text.toString()) }
    subscriptions.add(authorSubscription)
  }

  override fun onAuthorError() {
    bookAuthorInput.error = getString(R.string.error_book_author_malformed)
  }

  private fun registerNameInputSubscription() {
    val nameSubscription = bookNameInput.afterTextChangeEvents()
      .doOnNext {
        clearTextViewError(bookNameInput)
      }
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { event -> presenter.onNameChange(event.view.text.toString()) }
    subscriptions.add(nameSubscription)
  }

  private fun clearTextViewError(input: TextView) {
    if (input.error != null) {
      input.error = null
    }
  }

  override fun onNameError() {
    bookNameInput.error = getString(R.string.error_book_name_malformed)
  }

  private fun registerCoverClickSubscription() {
    val coverSubscription = cover.clicks()
      .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { coverChooserDialog?.show() }
    subscriptions.add(coverSubscription)
  }

  private fun requestCoverImageFromGallery() {
    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
    getIntent.type = "image/*"

    val pickIntent = Intent(Intent.ACTION_PICK)
    pickIntent.setDataAndType(
      Media.EXTERNAL_CONTENT_URI,
      "image/*"
    )

    val chooserIntent = Intent.createChooser(getIntent, getString(R.string.cover_chooser_title))
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

    chooserIntent.resolveActivity(requireActivity().packageManager)?.also {
      startActivityForResult(chooserIntent, PICK_IMAGE)
    }
  }

  private fun requestCoverImageFromCamera() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
      // Ensure that there's a camera activity to handle the intent
      takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
        // Create the File where the photo should go
        val photoFile: File? = try {
          presenter.createImageFile(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        } catch (ex: IOException) {
          null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
          val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            it
          )
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
          startActivityForResult(takePictureIntent, TAKE_PHOTO)
        }
      }
    }
  }

  override fun onCoverChosen(coverUri: Uri?) {
    GlideApp.with(this)
      .load(coverUri)
      .transition(withCrossFade())
      .into(cover)
  }

  override fun showCover() {
    if (cover.visibility == View.INVISIBLE) {
      cover.visibility = View.VISIBLE
    }
  }

  override fun askUserToProvideDefaultCity() {
    MaterialDialog(requireContext())
      .title(R.string.enter_city_title)
      .message(R.string.error_enter_city_content)
      .input(hintRes = R.string.city_hint, callback =
      { _, input -> presenter.saveCity(input.toString()) })
      .show()
  }

  override fun onReleased(newKey: String) {
    val dialog = MaterialDialog(requireContext())
      .title(R.string.book_saved_dialog_title)
      .customView(R.layout.book_sticker_layout)
      .positiveButton(R.string.ok) { dialog ->
        renderSticker(dialog.getCustomView().findViewById(R.id.sticker))
        listener.onBookReleased(newKey)
      }
      .onCancel { dialog ->
        renderSticker(dialog.getCustomView().findViewById(R.id.sticker))
        clearView()
      }

    prepareDialog(dialog.getCustomView().findViewById(R.id.sticker), newKey)
    dialog.show()
  }

  private fun clearView() {
    bookNameInput.text = ""
    bookAuthorInput.text = ""
    bookPositionInput.text = ""
    bookDescriptionInput.text = ""
    cover.setImageResource(R.drawable.ic_add_a_photo)
    cover.visibility = View.INVISIBLE
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
    sticker.setBackgroundColor(ContextCompat.getColor(sticker.context, R.color.white))
    val stickerBitmap = Bitmap.createBitmap(sticker.width, sticker.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(stickerBitmap)
    sticker.draw(canvas)
    presenter.saveSticker(stickerBitmap, stickerName, stickerDescription)
  }

  private fun resolveUserCity(): Observable<String> {
    return permissions.request(
      Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
    )
      .flatMapMaybe { presenter.resolveUserCity() }
  }

  companion object {
    private const val PICK_IMAGE: Int = 11
    private const val TAKE_PHOTO: Int = 22
  }
}
