package com.bookcrossing.mobile.util.adapters;

import android.view.View;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.util.listeners.BookListener;

import butterknife.OnClick;

public class StashedBookViewHolder extends BooksViewHolder {
    public StashedBookViewHolder(View view) {
        super(view);
    }

    public void load() {
        loadCover();
    }

    @OnClick(R.id.cover)
    public void onCoverClick() {
        ((BookListener) itemView.getContext()).onBookSelected(key);
    }
}
