package com.bookcrossing.mobile.util.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.bookcrossing.mobile.R;

import butterknife.BindView;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 05.12.2016.
 *
 * Stub holder for showing progress
 */

public class ProgressViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.progress)
    ProgressBar progressBar;

    public ProgressViewHolder(View view) {
        super(view);
    }

    public void bind() {
        this.progressBar.setIndeterminate(true);
    }

}
