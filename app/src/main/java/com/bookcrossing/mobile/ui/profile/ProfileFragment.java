package com.bookcrossing.mobile.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.modules.GlideApp;
import com.bookcrossing.mobile.presenters.ProfilePresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.AcquiredBooksViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import butterknife.BindView;
import moxy.presenter.InjectPresenter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ProfileFragment extends BaseFragment implements ProfileView {

  @InjectPresenter public ProfilePresenter presenter;

  @BindView(R.id.profile_image) public ImageView profileImage;

  @BindView(R.id.acquiredBooksList) public RecyclerView acquiredBooksList;

  public ProfileFragment() {
    // Required empty public constructor
  }

  private FirebaseRecyclerAdapter<Book, AcquiredBooksViewHolder> adapter;

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_profile, container, false);
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupAcquiredBookList();
    if (presenter.isAuthenticated()) {
      adapter.startListening();
      GlideApp.with(this)
        .load(presenter.getPhotoUrl())
        .transition(withCrossFade())
        .into(profileImage);
    } else {
      authenticate();
    }
  }

  private void setupAcquiredBookList() {
    LinearLayoutManager llm = new LinearLayoutManager(getActivity());
    acquiredBooksList.setLayoutManager(llm);
    adapter = new FirebaseRecyclerAdapter<Book, AcquiredBooksViewHolder>(
      new FirebaseRecyclerOptions.Builder<Book>().setQuery(presenter.getAcquiredBooks(), Book.class)
        .setLifecycleOwner(getViewLifecycleOwner())
        .build()) {
      @NonNull @Override
      public AcquiredBooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.acquired_book_list_item, parent, false);
        return new AcquiredBooksViewHolder(view);
      }

      @Override
      protected void onBindViewHolder(@NonNull AcquiredBooksViewHolder holder, int position,
        @NonNull Book model) {
        holder.setKey(this.getRef(position).getKey());
        holder.bind(model);
      }
    };
    acquiredBooksList.setAdapter(adapter);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (presenter.isAuthenticated()) {
      adapter.stopListening();
    }
  }
}
