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

package com.bookcrossing.mobile.ui.stash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.presenters.StashPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.StashedBookViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import butterknife.BindView;
import moxy.presenter.InjectPresenter;

public class StashFragment extends BaseFragment implements StashView {

  public static final int STASH_COLUMNS = 3;

  @InjectPresenter public StashPresenter presenter;

  @BindView(R.id.stash_rv) public RecyclerView rv;

  private FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder> adapter;

  public StashFragment() {
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_stash, container, false);
  }

  @Override
  public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (presenter.isAuthenticated()) {
      setupStash();
    } else {
      authenticate();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (adapter != null) {
      adapter.stopListening();
    }
  }

  private void setupStash() {
    RecyclerView.LayoutManager gridLayoutManager =
      new GridLayoutManager(getActivity(), STASH_COLUMNS);
    rv.setLayoutManager(gridLayoutManager);

    adapter = new FirebaseRecyclerAdapter<Boolean, StashedBookViewHolder>(
      new FirebaseRecyclerOptions.Builder<Boolean>().setQuery(presenter.getStashedBooks(),
        Boolean.class).setLifecycleOwner(getViewLifecycleOwner()).build()) {
      @NonNull @Override
      public StashedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.stash_item, parent, false);
        return new StashedBookViewHolder(view);
      }

      @Override protected void onBindViewHolder(@NonNull StashedBookViewHolder holder, int position,
        @NonNull Boolean model) {
        holder.setKey(this.getRef(position).getKey());
        holder.loadCover();
      }
    };

    rv.setAdapter(adapter);
    adapter.startListening();
  }
}
