package com.bookcrossing.mobile.ui.main;

import android.Manifest;
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
import com.afollestad.materialdialogs.MaterialDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.MainPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.BooksViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends BaseFragment implements MainView {

  @BindView(R.id.books_rv) public RecyclerView rv;

  @BindView(R.id.addBookButton) public FloatingActionButton fab;

  @BindView(R.id.adView) public AdView ad;

  @InjectPresenter public MainPresenter presenter;

  private FirebaseRecyclerAdapter<Book, BooksViewHolder> adapter;

  private RxPermissions permissions;

  @Override public int title() {
    return R.string.mainTitle;
  }

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

    setupBookList();

    subscriptions.add(RxView.clicks(fab).subscribe(o -> listener.onBookAdd()));

    loadAds();
  }

  private void loadAds() {
    AdRequest adRequest = new AdRequest.Builder().build();
    ad.loadAd(adRequest);
  }

  private void setupBookList() {
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
  }

  private void resolveCity() {
    permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION)
        .flatMap(granted -> {
          if (granted) {
            return presenter.resolveUserCity();
          }
          return Observable.empty();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(addresses -> {
          if (!addresses.isEmpty()) {
            presenter.saveCity(addresses);
          } else {
            askUserToProvideDefaultCity();
          }
        });
  }

  private void askUserToProvideDefaultCity() {
    new MaterialDialog.Builder(getContext()).title(R.string.enter_city_title)
        .content(R.string.enter_city_content)
        .input(R.string.city_hint, R.string.default_city, false,
            (dialog, input) -> presenter.saveCity(input.toString()))
        .show();
  }
}
