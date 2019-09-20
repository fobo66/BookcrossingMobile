/*
 *     Copyright 2019 Andrey Mukamolov
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.bookcrossing.mobile.util.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.modules.GlideApp;
import com.bookcrossing.mobile.presenters.BookItemPresenter;
import com.bookcrossing.mobile.ui.bookpreview.BookItemView;
import com.bookcrossing.mobile.util.listeners.BookListener;

import butterknife.BindView;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenterTag;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class BooksViewHolder extends MvpBaseViewHolder implements BookItemView {

  @InjectPresenter(tag = BookItemPresenter.TAG)
  public BookItemPresenter itemPresenter;

  protected String key;

  public BooksViewHolder(View view) {
    super(view);
  }

  @BindView(R.id.cover)
  ImageView cover;
  @BindView(R.id.book_name)
  TextView bookName;
  @BindView(R.id.author)
  TextView author;
  @BindView(R.id.current_place)
  TextView bookPlace;

  @ProvidePresenterTag(presenterClass = BookItemPresenter.class)
  String provideBookItemPresenterTag() {
    return BookItemPresenter.TAG;
  }

  @Override public void bind(@NonNull Book item) {
    loadCover();
    bookName.setText(item.getName());
    bookPlace.setText(item.getPositionName());
    author.setText(item.getAuthor());
  }

  private void loadCover() {
    GlideApp.with(itemView.getContext())
        .load(itemPresenter.resolveCover(key))
        .placeholder(R.drawable.ic_book_cover_placeholder).transition(withCrossFade())
        .thumbnail(0.6f)
        .into(cover);
  }

  @OnClick(R.id.card)
  public void onClick() {
    ((BookListener) itemView.getContext()).onBookSelected(key);
  }

  public void setKey(String key) {
    this.key = key;
  }
}
