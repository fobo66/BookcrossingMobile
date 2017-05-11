package com.bookcrossing.mobile.ui.bookpreview;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.BookPresenter;
import com.bookcrossing.mobile.util.Constants;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BookActivity extends MvpAppCompatActivity implements BookView {

    @InjectPresenter
    BookPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.cover)
    ImageView cover;

    @BindView(R.id.author)
    TextView author;

    @BindView(R.id.position)
    TextView position;

    @BindView(R.id.book_desc)
    TextView description;

    @BindView(R.id.acquire_button)
    Button acquireButton;

    @BindView(R.id.fab_like)
    FloatingActionButton favorite;

    private String key;
    private Disposable fabSubscription;
    private Disposable acquireSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            this.key = getIntent().getStringExtra(Constants.EXTRA_KEY);
            presenter.subscribeToBookReference(key);
            presenter.checkStashingState(key);
        }

        fabSubscription = RxView.clicks(favorite)
                .subscribe(new Consumer<Object>() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                presenter.handleBookStashing(key);
            }
        });

        acquireSubscription = RxView.clicks(acquireButton)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        handleAcquiring();
                    }
                });
    }

    private void handleAcquiring() {
        Intent acquireIntent = new Intent(Intent.ACTION_VIEW, presenter.buildBookUri(key));
        acquireIntent.putExtra(getString(R.string.extra_insideAppRequest), true);
        startActivity(acquireIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fabSubscription.dispose();
        acquireSubscription.dispose();
    }

    @Override
    public void onBookLoaded(Book book) {
        toolbar.setTitle(book.getName());
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(presenter.resolveCover(book))
                .crossFade()
                .thumbnail(0.6f)
                .into(cover);
        author.setText(book.getAuthor());
        position.setText(book.getPosition());
        description.setText(book.getDescription());
        if (book.isFree()) {
            acquireButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBookStashed() {
        favorite.setImageResource(R.drawable.ic_turned_in_white_24dp);
    }

    @Override
    public void onBookUnstashed() {
        favorite.setImageResource(R.drawable.ic_turned_in_not_white_24dp);
    }
}
