package com.bookcrossing.mobile.presenters;

import android.graphics.Bitmap;
import android.net.Uri;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Date;
import com.bookcrossing.mobile.ui.create.BookCreateView;
import com.crashlytics.android.Crashlytics;
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

@InjectViewState public class BookCreatePresenter extends BasePresenter<BookCreateView> {

  private Book book;
  private Uri tempCoverUri;

  public BookCreatePresenter() {
    super();
    this.book = new Book();
    book.setFree(true);
  }

  private void uploadCover(String key) {
    if (tempCoverUri != null && firebaseWrapper.getAuth().getCurrentUser() != null) {
      StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
      resolveCover(key).putFile(tempCoverUri, metadata);
    }
  }

  public void saveCoverTemporarily(FileData result) {
    tempCoverUri = Uri.fromFile(result.getFile());
    getViewState().onCoverChosen(tempCoverUri);
  }

  private Bitmap encodeBookAsQrCode(String contents, BarcodeFormat format, int img_width,
      int img_height) throws WriterException {
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

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  public void onNameChange(String name) {
    book.setName(name);
    getViewState().onNameChange();
  }

  public void onAuthorChange(String author) {
    book.setAuthor(author);
  }

  public void onPositionChange(String position) {
    book.setPositionName(position);
  }

  public void onDescriptionChange(String description) {
    book.setDescription(description);
  }

  public void publishBook() {
    book.setCity(getCity());
    setPublicationDate();
    final DatabaseReference newBookReference = books().push();
    newBookReference.setValue(book).addOnSuccessListener(aVoid -> {
      String key = newBookReference.getKey();
      uploadCover(key);
      getViewState().onReleased(key);
    }).addOnFailureListener(e -> {
      Crashlytics.logException(e);
      getViewState().onFailedToRelease();
    });
  }

  private void setPublicationDate() {
    Calendar calendar = Calendar.getInstance();
    Date date = new Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), new java.util.Date().getTime());
    book.setWentFreeAt(date);
  }

  public Bitmap generateQrCode(String key) {
    try {
      return encodeBookAsQrCode(buildBookUri(key).toString(), BarcodeFormat.QR_CODE, 450, 450);
    } catch (WriterException e) {
      e.printStackTrace();
      Crashlytics.logException(e);
      return null;
    }
  }
}
