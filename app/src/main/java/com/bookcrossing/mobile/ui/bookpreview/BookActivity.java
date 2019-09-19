package com.bookcrossing.mobile.ui.bookpreview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.modules.GlideApp;
import com.bookcrossing.mobile.presenters.BookPresenter;
import com.bookcrossing.mobile.ui.main.MainActivity;
import com.bookcrossing.mobile.ui.map.MapActivity;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.adapters.PlacesHistoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding3.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class BookActivity extends MvpAppCompatActivity
  implements BookView, Toolbar.OnMenuItemClickListener {

  @InjectPresenter public BookPresenter presenter;

  @BindView(R.id.toolbar) public Toolbar toolbar;

  @BindView(R.id.cover) public ImageView cover;

  @BindView(R.id.author) public TextView author;

  @BindView(R.id.positionName) public TextView position;

  @BindView(R.id.book_desc) public TextView description;

  @BindView(R.id.timestamp) public RelativeTimeTextView wentFree;

  @BindView(R.id.placesHistory) public RecyclerView placesHistory;

  @BindView(R.id.acquire_button) public Button acquireButton;

  @BindView(R.id.fab_like) public FloatingActionButton favorite;

  private String key;
  private Disposable fabSubscription;
  private Disposable acquireSubscription;
  private Disposable positionNameSubscription;
  private FirebaseRecyclerAdapter<Coordinates, PlacesHistoryViewHolder> adapter;
  private Coordinates currentBookPosition;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book);
    ButterKnife.bind(this);

    setupToolbar();

    if (getIntent() != null) {
      key = getIntent().getStringExtra(Constants.EXTRA_KEY);
      presenter.subscribeToBookReference(key);
      presenter.checkStashingState(key);
    }

    setupPlacesHistory();

    fabSubscription = RxView.clicks(favorite).subscribe(o -> presenter.handleBookStashing(key));

    acquireSubscription = RxView.clicks(acquireButton).subscribe(o -> handleAcquiring());

    positionNameSubscription =
      RxView.clicks(position).subscribe(o -> goToPosition(currentBookPosition));
  }

  private void setupToolbar() {
    toolbar.inflateMenu(R.menu.menu_book);
    toolbar.setOnMenuItemClickListener(this);
    toolbar.setNavigationIcon(R.drawable.ic_back);
    toolbar.setNavigationOnClickListener(view -> onBackPressed());
  }

  public void goToPosition(Coordinates coordinates) {
    Intent intent = new Intent(this, MapActivity.class);
    intent.putExtra(Constants.EXTRA_COORDINATES, coordinates);
    startActivity(intent);
  }

  private void setupPlacesHistory() {
    RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
    placesHistory.setLayoutManager(llm);
    adapter = new FirebaseRecyclerAdapter<Coordinates, PlacesHistoryViewHolder>(
      new FirebaseRecyclerOptions.Builder<Coordinates>().setQuery(presenter.getPlacesHistory(key),
        Coordinates.class).setLifecycleOwner(this).build()) {
      @NonNull @Override
      public PlacesHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.places_history_list_item, parent, false);
        return new PlacesHistoryViewHolder(view);
      }

      @Override
      protected void onBindViewHolder(@NonNull PlacesHistoryViewHolder holder, int position,
        @NonNull Coordinates model) {
        holder.bind(this.getRef(position).getKey(), model);
      }
    };
    placesHistory.setAdapter(adapter);
    adapter.startListening();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    fabSubscription.dispose();
    acquireSubscription.dispose();
    positionNameSubscription.dispose();
    adapter.stopListening();
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    if (item.getItemId() == R.id.menu_action_report) {
      presenter.reportAbuse(key);
      return true;
    }
    return false;
  }

  private void handleAcquiring() {
    Intent acquireIntent = new Intent(Intent.ACTION_VIEW, presenter.buildBookUri(key));
    acquireIntent.putExtra(getString(R.string.extra_insideAppRequest), true);
    startActivity(acquireIntent);
  }

  @Override public void onBookLoaded(Book book) {
    toolbar.setTitle(book.getName());
    GlideApp.with(this)
      .load(presenter.resolveCover(key))
      .transition(withCrossFade())
      .thumbnail(0.6f)
      .into(cover);
    author.setText(book.getAuthor());
    position.setText(String.format("%s, %s", book.getCity(), book.getPositionName()));
    wentFree.setReferenceTime(book.getWentFreeAt().getTimestamp());
    description.setText(book.getDescription());
    if (book.isFree()) {
      acquireButton.setVisibility(View.VISIBLE);
    }
    currentBookPosition = book.getPosition();
  }

  @Override public void onErrorToLoadBook() {
    new AlertDialog.Builder(this).setMessage(R.string.failed_to_load_book_message)
      .setTitle(R.string.error_dialog_title)
      .setPositiveButton(R.string.ok,
        (dialogInterface, i) -> startActivity(new Intent(BookActivity.this, MainActivity.class)))
      .show();
  }

  @Override public void onBookStashed() {
    favorite.setImageResource(R.drawable.ic_turned_in_white_24dp);
  }

  @Override public void onBookUnstashed() {
    favorite.setImageResource(R.drawable.ic_turned_in_not_white_24dp);
  }

  @Override public void onAbuseReported() {
    Toast.makeText(this, R.string.report_abuse_success, Toast.LENGTH_SHORT).show();
  }
}
