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
        .placeholder(R.drawable.ic_account_circle_black_24dp)
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
