package com.bookcrossing.mobile.ui.bookpreview;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.presenters.BookPresenter;
import com.bookcrossing.mobile.ui.main.MainActivity;
import com.bookcrossing.mobile.ui.map.MapActivity;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.adapters.PlacesHistoryViewHolder;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.crash.FirebaseCrash;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BookActivity extends MvpAppCompatActivity implements BookView {

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
  private boolean reportWasSent = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book);
    ButterKnife.bind(this);

    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    if (getIntent() != null) {
      this.key = getIntent().getStringExtra(Constants.EXTRA_KEY);
      presenter.subscribeToBookReference(key);
      presenter.checkStashingState(key);
    }

    setupPlacesHistory();

    fabSubscription = RxView.clicks(favorite).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        presenter.handleBookStashing(key);
      }
    });

    acquireSubscription = RxView.clicks(acquireButton).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        handleAcquiring();
      }
    });

    positionNameSubscription = RxView.clicks(position).subscribe(new Consumer<Object>() {
      @Override public void accept(@NonNull Object o) throws Exception {
        goToPosition(currentBookPosition);
      }
    });
  }

  public void goToPosition(Coordinates coordinates) {
    Intent intent = new Intent(this, MapActivity.class);
    intent.putExtra(Constants.EXTRA_COORDINATES, coordinates);
    startActivity(intent);
  }

  private void setupPlacesHistory() {
    RecyclerView.LayoutManager llm = new LinearLayoutManager(this);
    placesHistory.setLayoutManager(llm);
    adapter = new FirebaseRecyclerAdapter<Coordinates, PlacesHistoryViewHolder>(Coordinates.class,
        R.layout.places_history_list_item, PlacesHistoryViewHolder.class,
        presenter.getPlacesHistory(key)) {
      @Override
      protected void populateViewHolder(PlacesHistoryViewHolder viewHolder, Coordinates coordinates,
          int position) {
        viewHolder.bind(this.getRef(position).getKey(), coordinates);
      }
    };
    placesHistory.setAdapter(adapter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    fabSubscription.dispose();
    acquireSubscription.dispose();
    positionNameSubscription.dispose();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_book, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_action_report) {
      reportAbuse();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    if (reportWasSent) {
      menu.findItem(R.id.menu_action_report).setVisible(false);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  private void reportAbuse() {
    FirebaseCrash.report(
        new Exception(String.format("Users complaining to book %s. Consider to check it", key)));
    Toast.makeText(this, "Your report was sent", Toast.LENGTH_SHORT).show();
    reportWasSent = true;
  }

  private void handleAcquiring() {
    Intent acquireIntent = new Intent(Intent.ACTION_VIEW, presenter.buildBookUri(key));
    acquireIntent.putExtra(getString(R.string.extra_insideAppRequest), true);
    startActivity(acquireIntent);
  }

  @Override public void onBookLoaded(Book book) {
    toolbar.setTitle(book.getName());
    Glide.with(this)
        .using(new FirebaseImageLoader())
        .load(presenter.resolveCover(key))
        .crossFade()
        .placeholder(R.drawable.ic_book_cover_placeholder)
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
    new AlertDialog.Builder(this)
        .setMessage(R.string.failed_to_load_book_message)
        .setTitle(R.string.error_dialog_title)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialogInterface, int i) {
            startActivity(new Intent(BookActivity.this, MainActivity.class));
          }
        })
        .show();
  }

  @Override public void onBookStashed() {
    favorite.setImageResource(R.drawable.ic_turned_in_white_24dp);
  }

  @Override public void onBookUnstashed() {
    favorite.setImageResource(R.drawable.ic_turned_in_not_white_24dp);
  }
}
