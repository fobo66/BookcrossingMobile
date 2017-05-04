package com.bookcrossing.mobile.util.adapters;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpDelegate;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.arellomobile.mvp.presenter.ProvidePresenterTag;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.BookItemPresenter;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;
import com.bookcrossing.mobile.util.listeners.BookListener;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * (c) 2017 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 04.01.2017.
 */

public class BooksViewHolder extends BaseViewHolder implements BookItemView {

    private static final String TAG = "BooksViewHolder";

    @InjectPresenter(type = PresenterType.GLOBAL, tag = BookItemPresenter.TAG)
    BookItemPresenter itemPresenter;

    private MvpDelegate<? extends BaseViewHolder> mMvpDelegate;
    private String key;

    @ProvidePresenterTag(presenterClass = BookItemPresenter.class, type = PresenterType.GLOBAL)
    String provideRepositoryPresenterTag() {
        return BookItemPresenter.TAG;
    }

    @ProvidePresenter(type = PresenterType.GLOBAL)
    BookItemPresenter providePresenter() {
        BookItemPresenter presenter = new BookItemPresenter();
        presenter.setBook(this.book);
        return presenter;
    }

    @BindView(R.id.cover)
    ImageView cover;

    @BindView(R.id.book_name)
    TextView bookName;

    @BindView(R.id.author)
    TextView author;

    @BindView(R.id.current_place)
    TextView bookPlace;

    private Book book;

    public BooksViewHolder(View view) {
        super(view);
        getMvpDelegate().onCreate();
    }

    @Override
    public void bind(Book item) {
        this.book = item;
        itemPresenter.setBook(book);

        Glide.with(itemView.getContext())
                .using(new FirebaseImageLoader())
                .load(itemPresenter.resolveCover(book))
                .crossFade()
                .thumbnail(0.6f)
                .into(cover);
        bookName.setText(book.getName());
        bookPlace.setText(book.getPosition());
        author.setText(book.getAuthor());
    }

    @Override
    public void updateLikes() {

    }

    public MvpDelegate getMvpDelegate() {
        if(this.mMvpDelegate == null) {
            this.mMvpDelegate = new MvpDelegate<>(this);
        }

        return this.mMvpDelegate;
    }

    @OnClick(R.id.card)
    public void onClick() {
        ((BookListener) itemView.getContext()).onBookSelected(key);
    }

    public void setKey(String key) {
        this.key = key;
    }
}
