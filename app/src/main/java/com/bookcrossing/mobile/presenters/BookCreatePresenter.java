package com.bookcrossing.mobile.presenters;

import android.graphics.Bitmap;
import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Date;
import com.bookcrossing.mobile.ui.create.BookCreateView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


@InjectViewState
public class BookCreatePresenter extends BasePresenter<BookCreateView> {

    private static final String TAG = "BookCreatePresenter";

    private Book book;
    private Uri tempCoverUri;


    public BookCreatePresenter() {
        super();
        this.book = new Book();
        book.setFree(true);
    }

    private void uploadCover(String key) {
        if (tempCoverUri != null && firebaseWrapper.getAuth().getCurrentUser() != null) {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
            resolveCover(key).putFile(tempCoverUri, metadata);
        }

    }

    public void saveCoverTemporarly(FileData result) {
        tempCoverUri = Uri.fromFile(result.getFile());
        getViewState().OnCoverChosen(tempCoverUri);
    }

    private Bitmap encodeBookAsQrCode(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode(contents, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void OnNameChange(String name) {
        book.setName(name);
        getViewState().OnNameChange();
    }

    public void OnAuthorChange(String author) {
        book.setAuthor(author);
    }

    public void OnPositionChange(String position) {
        book.setPosition(position);
    }

    public void OnDescriptionChange(String description) {
        book.setDescription(description);
    }

    public void publishBook() {
        setPublicationDate();
        DatabaseReference newBookReference = books().push();
        newBookReference.setValue(book);
        String key = newBookReference.getKey();
        uploadCover(key);
        getViewState().OnReleased(key);
    }

    private void setPublicationDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                new java.util.Date().getTime());
        book.setWentFreeAt(date);
    }

    public Bitmap generateQrCode(String key) {
        try {
            return encodeBookAsQrCode(buildBookUri(key).toString(),
                    BarcodeFormat.QR_CODE, 350, 350);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
