package com.bookcrossing.mobile.util.adapters;

import android.view.View;
import com.arellomobile.mvp.MvpDelegate;

/**
 * Created by macbookpro2016 on 09.05.17.
 */

public class MvpBaseViewHolder extends BaseViewHolder {
  private MvpDelegate<? extends BaseViewHolder> mMvpDelegate;

  public MvpBaseViewHolder(View view) {
    super(view);
    getMvpDelegate().onCreate();
  }

  private MvpDelegate getMvpDelegate() {
    if (this.mMvpDelegate == null) {
      this.mMvpDelegate = new MvpDelegate<>(this);
    }

    return this.mMvpDelegate;
  }
}
