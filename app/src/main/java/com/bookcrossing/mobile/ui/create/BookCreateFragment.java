package com.bookcrossing.mobile.ui.create;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.BindString;
import butterknife.BindView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.modules.GlideApp;
import com.bookcrossing.mobile.presenters.BookCreatePresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.Constants;
import com.jakewharton.rxbinding3.view.RxView;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class BookCreateFragment extends BaseFragment implements BookCreateView {

  @InjectPresenter public BookCreatePresenter presenter;

  @BindView(R.id.cover) public ImageView cover;

  @BindView(R.id.input_name) public TextView bookNameInput;

  @BindView(R.id.input_author) public TextView bookAuthorInput;

  @BindView(R.id.input_position) public TextView bookPositionInput;

  @BindView(R.id.input_description) public TextView bookDescriptionInput;

    @BindView(R.id.publish_book)
    public Button releaseButton;

  @BindString(R.string.rendered_sticker_name) public String stickerName;

  @BindString(R.string.rendered_sticker_description) public String stickerDescription;

  private MaterialDialog coverChooserDialog;

  @Override public int title() {
    return R.string.add_new_book_title;
  }

  public BookCreateFragment() {
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_book_create, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

      buildCoverChooserDialog();
    registerSubscriptions();
  }

    private void buildCoverChooserDialog() {
        coverChooserDialog = new MaterialDialog.Builder(requireContext()).title("Choose source")
                .items(R.array.cover_chooser_dialog_items)
                .itemsCallback((dialog, itemView, position, text) -> {
                    Observable<Response<BookCreateFragment, FileData>> chooserObservable;
                    if (position == 0) {
                        chooserObservable = requestCoverImageFromGallery();
                    } else {
                        chooserObservable = requestCoverImageFromCamera();
                    }
                    subscriptions.add(chooserObservable.subscribe(result -> {
                        if (result.resultCode() == RESULT_OK) {
                            result.targetUI().presenter.saveCoverTemporarily(result.data());
                        }
                    }));
                })
                .build();
    }

  private void registerSubscriptions() {
    registerBookSubscriptions();
      registerPublishButtonEnableSubscription();
    registerPublishButtonClickSubscription();
  }

  private void registerBookSubscriptions() {
    registerCoverClickSubscription();
    registerNameInputSubscription();
    registerAuthorInputSubscription();
    registerPositionInputSubscription();
    registerDescriptionInputSubscription();
  }

  private void registerPublishButtonClickSubscription() {
      Disposable publishSubscription = RxView.clicks(releaseButton)
              .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(o -> publishBook());
    subscriptions.add(publishSubscription);
  }

    private void registerPublishButtonEnableSubscription() {
        Disposable publishSubscription = Observable.zip(
                RxTextView.textChanges(bookNameInput),
                RxTextView.textChanges(bookAuthorInput),
                RxTextView.textChanges(bookPositionInput),
                RxTextView.textChanges(bookDescriptionInput),
                (name, author, position, description) ->
                        !TextUtils.isEmpty(name) &&
                                !TextUtils.isEmpty(author) &&
                                !TextUtils.isEmpty(position) &&
                                !TextUtils.isEmpty(description)
        )
                .subscribe(enabled -> releaseButton.setEnabled(enabled));
        subscriptions.add(publishSubscription);
    }

  private void registerDescriptionInputSubscription() {
    Disposable descriptionSubscription = RxTextView.afterTextChangeEvents(bookDescriptionInput)
            .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(event -> !event.view().getText().toString().contains(Constants.PROHIBITED_SYMBOLS))
        .subscribe(event -> presenter.onDescriptionChange(event.view().getText().toString()));
    subscriptions.add(descriptionSubscription);
  }

  private void registerPositionInputSubscription() {
    Disposable positionSubscription = RxTextView.afterTextChangeEvents(bookPositionInput)
            .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(event -> !event.view().getText().toString().contains(Constants.PROHIBITED_SYMBOLS))
        .subscribe(event -> presenter.onPositionChange(event.view().getText().toString()));
    subscriptions.add(positionSubscription);
  }

  private void registerAuthorInputSubscription() {
    Disposable authorSubscription = RxTextView.afterTextChangeEvents(bookAuthorInput)
            .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
            .filter(event -> !event.view().getText().toString().contains(Constants.PROHIBITED_SYMBOLS))
        .subscribe(event -> presenter.onAuthorChange(event.view().getText().toString()));
    subscriptions.add(authorSubscription);
  }

  private void registerNameInputSubscription() {
    Disposable nameSubscription = RxTextView.afterTextChangeEvents(bookNameInput)
            .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
        .filter(event -> {
          String textFieldValue = event.view().getText().toString();
            return !textFieldValue.contains(Constants.PROHIBITED_SYMBOLS) && !textFieldValue.isEmpty();
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(event -> presenter.onNameChange(event.view().getText().toString()));
    subscriptions.add(nameSubscription);
  }

  private void registerCoverClickSubscription() {
      Disposable coverSubscription = RxView.clicks(cover)
              .debounce(Constants.DEFAULT_DEBOUNCE_TIMEOUT, TimeUnit.MILLISECONDS)
              .subscribe(o -> coverChooserDialog.show());
    subscriptions.add(coverSubscription);
  }

  private Observable<Response<BookCreateFragment, FileData>> requestCoverImageFromGallery() {
    return RxPaparazzo.single(this)
        .usingGallery()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Response<BookCreateFragment, FileData>> requestCoverImageFromCamera() {
    return RxPaparazzo.single(this)
        .usingCamera()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private void publishBook() {
      releaseButton.setEnabled(false);
    presenter.publishBook();
  }

  @Override public void onCoverChosen(Uri coverUri) {
    GlideApp.with(this).load(coverUri).transition(withCrossFade()).into(cover);
  }

  @Override public void onNameChange() {
    if (cover.getVisibility() == View.GONE) {
      cover.setVisibility(View.VISIBLE);
    }
  }

  @Override public void onReleased(final String newKey) {
    MaterialDialog dialog =
            new MaterialDialog.Builder(requireContext()).title(R.string.book_saved_dialog_title)
            .customView(R.layout.book_sticker_layout, true)
            .positiveText(R.string.ok)
            .onPositive((dialog1, which) -> {
              renderSticker(dialog1.getCustomView().findViewById(R.id.sticker));
              getActivity().getSupportFragmentManager().popBackStack();
              listener.onBookReleased(newKey);
            })
            .build();

    prepareDialog(dialog.getCustomView().findViewById(R.id.sticker), newKey);
    dialog.show();
  }

  @Override public void onFailedToRelease() {
      new MaterialDialog.Builder(requireContext()).content(R.string.failed_to_release_book_message)
        .title(R.string.error_dialog_title)
        .positiveText(R.string.ok)
        .onPositive((dialog, which) -> dialog.dismiss())
        .show();
  }

  private void prepareDialog(View stickerView, String key) {
    ImageView qrCode = stickerView.findViewById(R.id.qr_code);
    TextView keyView = stickerView.findViewById(R.id.sticker_book_key);
    qrCode.setImageBitmap(presenter.generateQrCode(key));
    keyView.setText(key);
  }

  private void renderSticker(View sticker) {
      sticker.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
      Bitmap stickerBitmap =
        Bitmap.createBitmap(sticker.getWidth(), sticker.getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(stickerBitmap);
      sticker.draw(canvas);
      MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), stickerBitmap, stickerName,
        stickerDescription);
  }
}
