package com.bookcrossing.mobile.presenters

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.arellomobile.mvp.InjectViewState
import com.bookcrossing.mobile.models.Book
import com.bookcrossing.mobile.models.Date
import com.bookcrossing.mobile.ui.create.BookCreateView
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.StorageMetadata
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import java.util.*

@InjectViewState
class BookCreatePresenter : BasePresenter<BookCreateView>() {

    private val book: Book = Book()
    private var tempCoverUri: Uri? = null

    init {
        book.isFree = true
    }

    private fun uploadCover(key: String?) {
        if (tempCoverUri != null && firebaseWrapper.auth.currentUser != null) {
            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
            resolveCover(key).putFile(tempCoverUri!!, metadata)
        }
    }

    fun saveCoverTemporarily(result: FileData) {
        tempCoverUri = Uri.fromFile(result.file)
        viewState.onCoverChosen(tempCoverUri)
    }

    @Throws(WriterException::class)
    private fun encodeBookAsQrCode(contents: String?, format: BarcodeFormat, imgWidth: Int,
                                   imgHeight: Int): Bitmap? {
        if (contents == null) {
            return null
        }
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix
        try {
            bitMatrix = writer.encode(contents, format, imgWidth, imgHeight, hints)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) BLACK else WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun onNameChange(name: String) {
        book.name = name
        viewState.onNameChange()
    }

    fun onAuthorChange(author: String) {
        book.author = author
    }

    fun onPositionChange(position: String) {
        book.positionName = position
    }

    fun onDescriptionChange(description: String) {
        book.description = description
    }

    fun publishBook() {
        book.city = getCity()
        setPublicationDate()
        val newBookReference = books().push()
        newBookReference.setValue(book).addOnSuccessListener {
            val key = newBookReference.key
            uploadCover(key)
            viewState.onReleased(key)
        }.addOnFailureListener { e ->
            Crashlytics.logException(e)
            viewState.onFailedToRelease()
        }
    }

    private fun setPublicationDate() {
        val calendar = Calendar.getInstance()
        val date = Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), java.util.Date().time)
        book.wentFreeAt = date
    }

    fun generateQrCode(key: String): Bitmap? {
        return try {
            encodeBookAsQrCode(buildBookUri(key).toString(), BarcodeFormat.QR_CODE, 450, 450)
        } catch (e: WriterException) {
            e.printStackTrace()
            Crashlytics.logException(e)
            null
        }

    }

    fun saveSticker(sticker: Bitmap, stickerName: String, stickerDescription: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, stickerName)
            put(MediaStore.Images.Media.DESCRIPTION, stickerDescription)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, sticker.width)
            put(MediaStore.Images.Media.HEIGHT, sticker.height)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = systemServicesWrapper.app.contentResolver
        val collection = MediaStore.Images.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val item = resolver.insert(collection, values)

        resolver.openOutputStream(item!!).use { stream ->
            sticker.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(item, values, null, null)
    }
}
