package com.bookcrossing.mobile.util.adapters;

import android.support.v7.app.AlertDialog;
import android.view.View;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.listeners.BookListener;

public class StashedBookViewHolder extends BooksViewHolder {
  public StashedBookViewHolder(View view) {
    super(view);
  }

  public void load() {
    loadCover();
  }

  @OnClick(R.id.cover) public void onCoverClick() {
    ((BookListener) itemView.getContext()).onBookSelected(key);
  }

  @OnLongClick(R.id.cover) public boolean onLongClick() {
    new AlertDialog.Builder(itemView.getContext()).setItems(new String[] { "Remove from stash" },
        (dialogInterface, i) -> itemPresenter.unstashCurrentBook(key))
        .show();
    return true;
  }
}
