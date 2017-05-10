package com.bookcrossing.mobile.ui.profile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.ProfilePresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.AcquiredBooksViewHolder;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import butterknife.BindView;

public class ProfileFragment extends BaseFragment implements ProfileView {

    @InjectPresenter
    ProfilePresenter presenter;

    @BindView(R.id.profile_image)
    ImageView profileImage;

    @BindView(R.id.acquiredBooksList)
    RecyclerView acquiredBooksList;

    public ProfileFragment() {
        // Required empty public constructor
    }

    private FirebaseRecyclerAdapter<Book, AcquiredBooksViewHolder> adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        acquiredBooksList.setLayoutManager(llm);
        adapter = new FirebaseRecyclerAdapter<Book, AcquiredBooksViewHolder>(Book.class, R.layout.acquired_book_list_item,
                AcquiredBooksViewHolder.class, presenter.getAcquiredBooks()) {
            @Override
            protected void populateViewHolder(AcquiredBooksViewHolder viewHolder, Book model, int position) {
                viewHolder.bind(model);
                viewHolder.setKey(this.getRef(position).getKey());
            }
        };
        acquiredBooksList.setAdapter(adapter);


        Glide.with(this)
                .fromUri()
                .crossFade()
                .load(presenter.getPhotoUrl())
                .into(profileImage);
    }
}
