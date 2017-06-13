package com.bookcrossing.mobile.ui.main;

import android.Manifest;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.MainPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.BooksViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.List;

public class MainFragment extends BaseFragment implements MainView {

  @BindView(R.id.books_rv) RecyclerView rv;

  @BindView(R.id.addBookButton) FloatingActionButton fab;

  @InjectPresenter MainPresenter presenter;

  private FirebaseRecyclerAdapter<Book, BooksViewHolder> adapter;

  private RxPermissions permissions;

  public MainFragment() {
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    adapter.cleanup();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    permissions = new RxPermissions(getActivity());

    resolveCity();

    RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity());
    rv.setLayoutManager(llm);
    adapter =
        new FirebaseRecyclerAdapter<Book, BooksViewHolder>(Book.class, R.layout.book_list_item_main,
            BooksViewHolder.class, presenter.getBooks()) {
          @Override
          protected void populateViewHolder(BooksViewHolder viewHolder, Book model, int position) {
            viewHolder.setKey(this.getRef(position).getKey());
            viewHolder.bind(model);
          }
        };

    rv.setAdapter(adapter);
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(rv);

    subscriptions.add(RxView.clicks(fab).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        listener.onBookAdd();
      }
    }));
  }

  private void resolveCity() {
    subscriptions.add(permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION)
        .flatMap(new Function<Boolean, ObservableSource<List<Address>>>() {
          @Override public ObservableSource<List<Address>> apply(@NonNull Boolean granted)
              throws Exception {
            if (granted) {
              return presenter.resolveUserCity();
            }
            return Observable.empty();
          }
        })
        .subscribe(new Consumer<List<Address>>() {
          @Override public void accept(@NonNull List<Address> addresses) throws Exception {
            presenter.saveCity(addresses);
          }
        }));
  }
}
