package com.bookcrossing.mobile.util.adapters;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.arellomobile.mvp.presenter.ProvidePresenterTag;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.modules.GlideApp;
import com.bookcrossing.mobile.presenters.BookItemPresenter;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;
import com.bookcrossing.mobile.util.listeners.BookListener;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Reusable ViewHolder. As for me, book representation across all RecyclerViews
 * is the same, but not everywhere we show all of the book's class fields
 * So, maybe this solution sucks, but it's ok for now.
 * If I find better solution, I'll refactor this.
 *
 * (c) 2017 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
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

  @ProvidePresenter(type = PresenterType.GLOBAL) BookItemPresenter providePresenter() {
    return new BookItemPresenter();
  }

  @Nullable @BindView(R.id.cover) ImageView cover;

  @Nullable @BindView(R.id.book_name) TextView bookName;

  @Nullable @BindView(R.id.author) TextView author;

  @Nullable @BindView(R.id.current_place) TextView bookPlace;

  @Override public void bind(Book item) {
    loadCover();
    bookName.setText(item.getName());
    bookPlace.setText(item.getPositionName());
    author.setText(item.getAuthor());
  }

  protected void loadCover() {
    GlideApp.with(itemView.getContext())
        .load(itemPresenter.resolveCover(key))
        .placeholder(R.drawable.ic_book_cover_placeholder).transition(withCrossFade())
        .thumbnail(0.6f)
        .into(cover);
  }

  @Optional @OnClick(R.id.card) public void onClick() {
    ((BookListener) itemView.getContext()).onBookSelected(key);
  }

  public void setKey(String key) {
    this.key = key;
  }
}
