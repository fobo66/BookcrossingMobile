package com.bookcrossing.mobile.ui.create;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.BookCreatePresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

public class BookCreateFragment extends BaseFragment implements BookCreateView {
  @InjectPresenter BookCreatePresenter presenter;

  @BindView(R.id.cover) ImageView cover;

  @BindView(R.id.input_name) TextView bookNameInput;

  @BindView(R.id.input_author) TextView bookAuthorInput;

  @BindView(R.id.input_position) TextView bookPositionInput;

  @BindView(R.id.input_description) TextView bookDescriptionInput;

  @BindView(R.id.publish_book) Button publishButton;

  @BindString(R.string.rendered_sticker_name) String stickerName;

  @BindString(R.string.rendered_sticker_description) String stickerDescription;

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

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    coverChooserDialog = new MaterialDialog.Builder(getContext()).title("Choose source")
        .items(R.array.cover_chooser_dialog_items)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override public void onSelection(MaterialDialog dialog, View itemView, int position,
              CharSequence text) {
            Observable<Response<BookCreateFragment, FileData>> chooserObservable;
            if (position == 0) {
              chooserObservable = requestCoverImageFromGallery();
            } else {
              chooserObservable = requestCoverImageFromCamera();
            }
            subscriptions.add(
                chooserObservable.subscribe(new Consumer<Response<BookCreateFragment, FileData>>() {
                  @Override
                  public void accept(@NonNull Response<BookCreateFragment, FileData> result)
                      throws Exception {
                    if (result.resultCode() == RESULT_OK) {
                      result.targetUI().presenter.saveCoverTemporarily(result.data());
                    }
                  }
                }));
          }
        })
        .build();
    registerSubscriptions();
  }

  private void registerSubscriptions() {
    registerBookSubscriptions();
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
    Disposable publishSubscription = RxView.clicks(publishButton).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        publishBook();
      }
    });
    subscriptions.add(publishSubscription);
  }

  private void registerDescriptionInputSubscription() {
    Disposable descriptionSubscription = RxTextView.afterTextChangeEvents(bookDescriptionInput)
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter(new Predicate<TextViewAfterTextChangeEvent>() {
          @Override public boolean test(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            return !event.view().getText().toString().contains("*#[]?");
          }
        })
        .subscribe(new Consumer<TextViewAfterTextChangeEvent>() {
          @Override public void accept(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            presenter.OnDescriptionChange(event.view().getText().toString());
          }
        });
    subscriptions.add(descriptionSubscription);
  }

  private void registerPositionInputSubscription() {
    Disposable positionSubscription = RxTextView.afterTextChangeEvents(bookPositionInput)
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter(new Predicate<TextViewAfterTextChangeEvent>() {
          @Override public boolean test(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            return !event.view().getText().toString().contains("*#[]?");
          }
        })
        .subscribe(new Consumer<TextViewAfterTextChangeEvent>() {
          @Override public void accept(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            presenter.OnPositionChange(event.view().getText().toString());
          }
        });
    subscriptions.add(positionSubscription);
  }

  private void registerAuthorInputSubscription() {
    Disposable authorSubscription = RxTextView.afterTextChangeEvents(bookAuthorInput)
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter(new Predicate<TextViewAfterTextChangeEvent>() {
          @Override public boolean test(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            return !event.view().getText().toString().contains("*#[]?");
          }
        })
        .subscribe(new Consumer<TextViewAfterTextChangeEvent>() {
          @Override public void accept(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            presenter.OnAuthorChange(event.view().getText().toString());
          }
        });
    subscriptions.add(authorSubscription);
  }

  private void registerNameInputSubscription() {
    Disposable nameSubscription = RxTextView.afterTextChangeEvents(bookNameInput)
        .debounce(300, TimeUnit.MILLISECONDS)
        .filter(new Predicate<TextViewAfterTextChangeEvent>() {
          @Override public boolean test(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            String textFieldValue = event.view().getText().toString();
            return !textFieldValue.contains("*#[]?") && !textFieldValue.isEmpty();
          }
        })
        .subscribe(new Consumer<TextViewAfterTextChangeEvent>() {
          @Override public void accept(@NonNull TextViewAfterTextChangeEvent event)
              throws Exception {
            presenter.OnNameChange(event.view().getText().toString());
          }
        });
    subscriptions.add(nameSubscription);
  }

  private void registerCoverClickSubscription() {
    Disposable coverSubscription = RxView.clicks(cover).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        coverChooserDialog.show();
      }
    });
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
    publishButton.setEnabled(false);
    presenter.publishBook();
  }

  @Override public void onCoverChosen(Uri coverUri) {
    Glide.with(this).fromUri().load(coverUri).crossFade().into(cover);
  }

  @Override public void onNameChange() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        if (cover.getVisibility() == View.GONE) {
          cover.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  @Override public void onReleased(final String newKey) {
    MaterialDialog dialog =
        new MaterialDialog.Builder(getContext()).title(R.string.book_saved_dialog_title)
            .customView(R.layout.book_sticker_layout, true)
            .positiveText(R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override
              public void onClick(@android.support.annotation.NonNull MaterialDialog dialog,
                  @android.support.annotation.NonNull DialogAction which) {
                renderSticker(dialog.getCustomView().findViewById(R.id.sticker));
                getActivity().getSupportFragmentManager().popBackStack();
                listener.onBookReleased(newKey);
              }
            })
            .build();

    prepareDialog(dialog.getCustomView().findViewById(R.id.sticker), newKey);
    dialog.show();
  }

  @Override public void onFailedToRelease() {
    new MaterialDialog.Builder(getContext()).content(R.string.failed_to_release_book_message)
        .title(R.string.error_dialog_title)
        .positiveText(R.string.ok)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@android.support.annotation.NonNull MaterialDialog dialog,
              @android.support.annotation.NonNull DialogAction which) {
            dialog.dismiss();
          }
        })
        .show();
  }

  private void prepareDialog(View stickerView, String key) {
    ImageView qrCode = stickerView.findViewById(R.id.qr_code);
    TextView keyView = stickerView.findViewById(R.id.sticker_book_key);
    qrCode.setImageBitmap(presenter.generateQrCode(key));
    keyView.setText(key);
  }

  private void renderSticker(View sticker) {
    sticker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
    Bitmap b =
        Bitmap.createBitmap(sticker.getWidth(), sticker.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    sticker.draw(c);
    MediaStore.Images.Media.insertImage(getContext().getContentResolver(), b, stickerName,
        stickerDescription);
  }
}
