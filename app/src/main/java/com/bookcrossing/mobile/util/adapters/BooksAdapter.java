/*
 *    Copyright 2019 Andrey Mukamolov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.util.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.arellomobile.mvp.MvpDelegate;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 05.12.2016.
 */

public class BooksAdapter extends MvpBaseAdapter {

  private static final int BOOK_VIEW_TYPE = 0;
  private static final int PROGRESS_VIEW_TYPE = 1;

  private boolean progress;

  private List<Book> items = Collections.emptyList();

  public BooksAdapter(MvpDelegate<?> parentDelegate) {
    super(parentDelegate, String.valueOf(0));
  }

  public void set(List<Book> items) {
    this.items = Collections.unmodifiableList(new ArrayList<>(items));
    notifyDataSetChanged();
  }

  public void add(List<Book> items) {
    int prevSize = this.items.size();
    if (items != null) {
      List<Book> list = new ArrayList<>(prevSize + items.size());
      list.addAll(this.items);
      list.addAll(items);
      this.items = Collections.unmodifiableList(list);
      notifyItemRangeInserted(prevSize, items.size());
    }
  }

  public void clear() {
    items.clear();
    notifyDataSetChanged();
  }

  @Override public int getItemViewType(int position) {
    return getItem(position) != null ? BOOK_VIEW_TYPE : PROGRESS_VIEW_TYPE;
  }

  @Override public int getItemCount() {
    return items.size() + (progress ? 1 : 0);
  }

  private Book getItem(int position) {
    return items.get(position);
  }

  @NonNull @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    RecyclerView.ViewHolder vh;

    if (viewType == BOOK_VIEW_TYPE) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.book_list_item_main, parent, false);
      vh = new BooksViewHolder(view);
    } else {
      View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_layout, parent, false);
      vh = new ProgressViewHolder(view);
    }

    return vh;
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof BooksViewHolder) {
      Book book = getItem(position);

      ((BooksViewHolder) holder).bind(book);
    } else if (holder instanceof ProgressViewHolder) {
      ((ProgressViewHolder) holder).bind();
    }
  }
}
