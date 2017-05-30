package com.bookcrossing.mobile.ui.stash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.StashPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.StashedBookViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import butterknife.BindView;

/**
 * Created by fobo66 on 18.05.17.
 */

public class StashFragment extends BaseFragment implements StashView {

    public static final int STASH_COLUMNS = 3;

    @InjectPresenter
    StashPresenter presenter;

    @BindView(R.id.stash_rv)
    RecyclerView rv;

    private FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder> adapter;

    public StashFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stash, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), STASH_COLUMNS);
        rv.setLayoutManager(gridLayoutManager);

        adapter = new FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder>(Boolean.class, R.layout.stash_item,
                StashedBookViewHolder.class, presenter.getStashedBooks()) {
            @Override
            protected void populateViewHolder(StashedBookViewHolder viewHolder, Boolean model, int position) {
                viewHolder.setKey(this.getRef(position).getKey());
                viewHolder.load();
            }
        };

        rv.setAdapter(adapter);
    }
}