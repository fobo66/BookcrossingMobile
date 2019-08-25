package com.bookcrossing.mobile.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import butterknife.BindView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.presenters.MainPresenter;
import com.bookcrossing.mobile.ui.base.BaseFragment;
import com.bookcrossing.mobile.util.adapters.BooksViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding3.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Maybe;
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

  @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    adapter.stopListening();
  }

  @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

      permissions = new RxPermissions(requireActivity());

    resolveCity();

    setupBookList();

    subscriptions.add(RxView.clicks(fab).subscribe(o -> listener.onBookAdd()));

    loadAds();
  }

  private void loadAds() {
    AdRequest.Builder adBuilder = new AdRequest.Builder();
    presenter.checkForConsent(adBuilder);
    AdRequest adRequest = adBuilder.build();
    ad.loadAd(adRequest);
  }

  private void setupBookList() {
    RecyclerView.LayoutManager llm = new LinearLayoutManager(getActivity());
    rv.setLayoutManager(llm);
    adapter = new FirebaseRecyclerAdapter<Book, BooksViewHolder>(
        new FirebaseRecyclerOptions.Builder<Book>().setQuery(presenter.getBooks(), Book.class)
            .build()) {
      @NonNull @Override
      public BooksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.book_list_item_main, parent, false);
        return new BooksViewHolder(view);
      }

      @Override protected void onBindViewHolder(@NonNull BooksViewHolder holder, int position,
          @NonNull Book model) {
        holder.setKey(this.getRef(position).getKey());
        holder.bind(model);
      }
    };

    rv.setAdapter(adapter);
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(rv);
    adapter.startListening();
  }

  private void resolveCity() {
    subscriptions.add(
        permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .flatMapMaybe(granted -> {
              if (granted) {
                return presenter.resolveUserCity();
              }
                  return Maybe.empty();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
                .subscribe(city -> {
                  if (!city.isEmpty()) {
                    presenter.saveCity(city);
              } else {
                askUserToProvideDefaultCity();
              }
            }));
  }

  private void askUserToProvideDefaultCity() {
      new MaterialDialog.Builder(requireContext()).title(R.string.enter_city_title)
        .content(R.string.enter_city_content)
        .input(R.string.city_hint, R.string.default_city, false,
            (dialog, input) -> presenter.saveCity(input.toString()))
        .show();
  }
}
