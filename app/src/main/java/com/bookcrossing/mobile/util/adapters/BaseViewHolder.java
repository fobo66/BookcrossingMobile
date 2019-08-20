package com.bookcrossing.mobile.util.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 05.12.2016.
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

  public BaseViewHolder(View view) {
    super(view);
    ButterKnife.bind(this, view);
  }
}
