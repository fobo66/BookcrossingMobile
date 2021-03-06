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

package com.bookcrossing.mobile.ui.releasebook

import android.app.Activity.RESULT_OK
import android.content.Context
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
import com.afollestad.materialdialogs.list.listItems
import com.bookcrossing.mobile.BuildConfig
import com.bookcrossing.mobile.R
import com.bookcrossing.mobile.code.BookStickerSaver
import com.bookcrossing.mobile.modules.GlideApp
import com.bookcrossing.mobile.modules.injector
import com.bookcrossing.mobile.presenters.BookReleasePresenter
import com.bookcrossing.mobile.ui.base.BaseFragment
import com.bookcrossing.mobile.ui.map.LocationPicker
import com.bookcrossing.mobile.util.DEFAULT_DEBOUNCE_TIMEOUT
import com.bookcrossing.mobile.util.ValidationResult.Invalid
import com.bookcrossing.mobile.util.ValidationResult.OK
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.github.florent37.runtimepermission.rx.RxPermissions
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import moxy.ktx.moxyPresenter
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Provider

/**
 * Screen for release new book
 */
class BookReleaseFragment : BaseFragment(), BookReleaseView {

  @Inject
  lateinit var presenterProvider: Provider<BookReleasePresenter>

  private val presenter: BookReleasePresenter by moxyPresenter { presenterProvider.get() }

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

  @BindView(R.id.pick_book_position_button)
  lateinit var pickBookPositionButton: Button

  @BindView(R.id.release_book)
  lateinit var releaseButton: MaterialButton

  @BindString(R.string.rendered_sticker_name)
  lateinit var stickerName: String

  @BindString(R.string.rendered_sticker_description)
  lateinit var stickerDescription: String

  private lateinit var permissions: RxPermissions

  override fun onAttach(context: Context) {
    injector.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_book_release, container, false)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    permissions = RxPermissions(this)

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

  private fun showCoverChooserDialog() {
    MaterialDialog(requireContext()).show {
      title(R.string.cover_chooser_title)
      listItems(R.array.cover_chooser_dialog_items, selection = { _, index, _ ->
        if (index == 0) {
          requestCoverImageFromGallery()
        } else {
          requestCoverImageFromCamera()
        }
      })
    }
  }

  private fun registerSubscriptions() {
    registerCoverClickSubscription()
    registerInputProcessingSubscription()
    registerPublishButtonEnableSubscription()
    registerReleaseButtonClickSubscription()
    registerPickLocationButtonClickSubscription()
  }

  private fun registerPickLocationButtonClickSubscription() {
    subscriptions.add(
      pickBookPositionButton.clicks()
        .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .flatMap {
          val locationPicker = LocationPicker()
          locationPicker.show(
            requireActivity().supportFragmentManager,
            "com.bookcrossing.mobile.ui.create.LocationPicker"
          )
          locationPicker.onBookLocationPicked()
        }
        .doOnNext { presenter.locationPicked(it) }
        .flatMapSingle { presenter.resolveCity(it) }
        .subscribe {
          presenter.saveCity(it)
        }
    )
  }

  private fun registerReleaseButtonClickSubscription() {
    val releaseSubscription = releaseButton.clicks()
      .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
      .flatMap { presenter.releaseBook() }
      .retry()
      .subscribe()
    subscriptions.add(releaseSubscription)
  }

  private fun registerPublishButtonEnableSubscription() {
    val publishSubscription =
      Observables.combineLatest(
        bookNameInput.textChanges(),
        bookAuthorInput.textChanges(),
        bookPositionInput.textChanges(),
        bookDescriptionInput.textChanges(),
        presenter.onLocationPicked()
      ) { name, author, position, description, isLocationPicked ->
        name.isNotBlank() &&
          author.isNotBlank() &&
          position.isNotBlank() &&
          description.isNotBlank() &&
          isLocationPicked
      }
        .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { enabled: Boolean -> releaseButton.isEnabled = enabled }
    subscriptions.add(publishSubscription)
  }

  private fun registerInputProcessingSubscription() {
    val nameSubscription = Observable.merge(
      bookNameInput.afterTextChangeEvents(),
      bookAuthorInput.afterTextChangeEvents(),
      bookPositionInput.afterTextChangeEvents(),
      bookDescriptionInput.afterTextChangeEvents()
    )
      .skip(4)
      .doOnNext {
        clearTextViewError(it.view)
      }
      .debounce(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { event ->
        val input = event.view.text.toString()
        when (val result = presenter.validateInput(input)) {
          is OK -> presenter.handleInputField(event.view.id, input)
          is Invalid -> event.view.error = getString(result.messageId)
        }
        showCover()
      }
    subscriptions.add(nameSubscription)
  }

  private fun clearTextViewError(input: TextView) {
    if (input.error != null) {
      input.error = null
    }
  }

  private fun registerCoverClickSubscription() {
    val coverSubscription = cover.clicks()
      .throttleFirst(DEFAULT_DEBOUNCE_TIMEOUT.toLong(), MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { showCoverChooserDialog() }
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
          presenter.createImageFile(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
          )
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
      .message(R.string.failed_to_release_book_message)
      .title(R.string.error_dialog_title)
      .positiveButton(R.string.ok) { dialog ->
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
    BookStickerSaver(
      requireContext().contentResolver
    ).saveSticker(
      stickerName,
      stickerDescription,
      stickerBitmap
    )
  }

  companion object {
    private const val PICK_IMAGE: Int = 11
    private const val TAKE_PHOTO: Int = 22
  }
}