package com.bookcrossing.mobile.util.adapters;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

import butterknife.BindView;
import butterknife.OnClick;

/**
 * (c) 2017 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 04.01.2017.
 */

public class BooksViewHolder extends MvpBaseViewHolder implements BookItemView {

    private static final String TAG = "BooksViewHolder";

    @InjectPresenter(type = PresenterType.GLOBAL, tag = BookItemPresenter.TAG)
    public BookItemPresenter itemPresenter;

    protected String key;

    public BooksViewHolder(View view) {
        super(view);
    }

    @ProvidePresenterTag(presenterClass = BookItemPresenter.class, type = PresenterType.GLOBAL)
    String provideRepositoryPresenterTag() {
        return BookItemPresenter.TAG;
    }

    @ProvidePresenter(type = PresenterType.GLOBAL)
    BookItemPresenter providePresenter() {
        return new BookItemPresenter();
    }

    @Nullable
    @BindView(R.id.cover)
    ImageView cover;

    @BindView(R.id.book_name)
    TextView bookName;

    @BindView(R.id.author)
    TextView author;

    @Nullable
    @BindView(R.id.current_place)
    TextView bookPlace;

    private Book book;

    @Override
    public void bind(Book item) {
        this.book = item;

        Glide.with(itemView.getContext())
                .using(new FirebaseImageLoader())
                .load(itemPresenter.resolveCover(key))
                .crossFade()
                .thumbnail(0.6f)
                .into(cover);
        bookName.setText(book.getName());
        bookPlace.setText(book.getPosition());
        author.setText(book.getAuthor());
    }

    @OnClick(R.id.card)
    public void onClick() {
        ((BookListener) itemView.getContext()).onBookSelected(key);
    }

    public void setKey(String key) {
        this.key = key;
    }
}
