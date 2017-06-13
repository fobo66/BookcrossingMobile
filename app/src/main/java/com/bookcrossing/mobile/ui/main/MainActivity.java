package com.bookcrossing.mobile.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.ui.InstantSearch;
import com.algolia.instantsearch.ui.utils.ItemClickSupport;
import com.algolia.instantsearch.ui.views.Hits;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.ui.base.BaseActivity;
import com.bookcrossing.mobile.ui.bookpreview.BookActivity;
import com.bookcrossing.mobile.ui.create.BookCreateFragment;
import com.bookcrossing.mobile.ui.map.MapActivity;
import com.bookcrossing.mobile.ui.profile.ProfileFragment;
import com.bookcrossing.mobile.ui.settings.SettingsFragment;
import com.bookcrossing.mobile.ui.stash.StashFragment;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.listeners.BookListener;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Collections;
import org.json.JSONException;

public class MainActivity extends BaseActivity
    implements BookListener, NavigationView.OnNavigationItemSelectedListener {

  @BindView(R.id.coord_layout) CoordinatorLayout coordinatorLayout;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.nav_view) NavigationView navigationView;

  @BindView(R.id.drawer_layout) DrawerLayout drawer;

  @BindView(R.id.hits) Hits hits;

  private ActionBarDrawerToggle drawerToggle;
  private Searcher searcher;
  private InstantSearch instantSearch;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    setupSearch();
    setSupportActionBar(toolbar);
    drawerToggle = setupDrawerToggle();
    drawer.addDrawerListener(drawerToggle);
    hits.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
      @Override public void onItemClick(RecyclerView recyclerView, int position, View v) {
        try {
          hits.setVisibility(View.GONE);
          onBookSelected(hits.get(position).getString("objectID"));
        } catch (JSONException e) {
          Snackbar.make(coordinatorLayout, "Cannot open book info", Snackbar.LENGTH_SHORT).show();
          e.printStackTrace();
        }
      }
    });
    navigationView.setNavigationItemSelectedListener(this);

    FirebaseAuth auth = FirebaseAuth.getInstance();
    if (auth.getCurrentUser() != null) {
      resolveNavigationToFragment(savedInstanceState);
    } else {
      startActivityForResult(AuthUI.getInstance()
          .createSignInIntentBuilder()
          .setAvailableProviders(Collections.singletonList(
              new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
          .build(), Constants.RC_SIGN_IN);
    }
  }

  private void resolveNavigationToFragment(Bundle savedInstanceState) {
    if (getIntent() != null) {
      String whereToGo = getIntent().getStringExtra(Constants.EXTRA_TARGET_FRAGMENT);
      if (whereToGo != null) {
        if (whereToGo.equalsIgnoreCase("BookCreateFragment")) {
          push(new BookCreateFragment());
        } else if (whereToGo.equalsIgnoreCase("ProfileFragment")) {
          push(new ProfileFragment());
        } else {
          push(new MainFragment());
        }
      } else {
        push(new MainFragment());
      }
    } else if (savedInstanceState != null) {
      pushRecentFragment();
    } else {
      push(new MainFragment());
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    instantSearch.registerSearchView(this, menu, R.id.menu_action_search);
    return true;
  }

  private ActionBarDrawerToggle setupDrawerToggle() {
    return new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open,
        R.string.drawer_close);
  }

  private void setupSearch() {
    searcher = new Searcher(getString(R.string.algolia_app_id), getString(R.string.algolia_api_key),
        getString(R.string.algolia_index_name));
    instantSearch = new InstantSearch(hits, searcher);
  }

  private void pushRecentFragment() {
    Fragment recentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    if (recentFragment != null) {
      push(recentFragment);
    }
  }

  public void push(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .addToBackStack(null)
        .replace(R.id.fragmentContainer, fragment)
        .commit();
  }

  public void replace(Fragment fragment) {
    getSupportFragmentManager().popBackStackImmediate(null,
        FragmentManager.POP_BACK_STACK_INCLUSIVE);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragmentContainer, fragment)
        .commit();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        drawer.openDrawer(GravityCompat.START);
        return true;
      case R.id.menu_action_search:
        if (hits.getVisibility() == View.GONE) {
          hits.setVisibility(View.VISIBLE);
        } else {
          hits.setVisibility(View.GONE);
        }
        return true;
      case R.id.menu_action_logout:
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override public void onComplete(@NonNull Task<Void> task) {
            finish();
          }
        });
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.nav_catalogue:
        push(new MainFragment());
        drawer.closeDrawer(navigationView);
        return true;
      case R.id.nav_stash:
        toolbar.setTitle(R.string.stash_fragment_heading);
        push(new StashFragment());
        drawer.closeDrawer(navigationView);
        return true;
      case R.id.nav_books_map:
        navigateToMap();
        return true;
      case R.id.nav_profile:
        push(new ProfileFragment());
        drawer.closeDrawer(navigationView);
        return true;
      case R.id.nav_settings:
        push(new SettingsFragment());
        drawer.closeDrawer(navigationView);
        return true;
    }

    return false;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.RC_SIGN_IN) {
      IdpResponse response = IdpResponse.fromResultIntent(data);
      if (resultCode == ResultCodes.OK) {
        push(new MainFragment());
        return;
      } else {

        if (response == null) {
          Snackbar.make(coordinatorLayout, getResources().getString(R.string.sign_in_cancelled),
              Snackbar.LENGTH_LONG).show();
          return;
        }

        if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
          Snackbar.make(coordinatorLayout,
              getResources().getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
              .show();
          return;
        }

        if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
          Snackbar.make(coordinatorLayout, getResources().getString(R.string.unknown_signin_error),
              Snackbar.LENGTH_LONG).show();
          return;
        }
      }

      Snackbar.make(coordinatorLayout, getResources().getString(R.string.sign_in_failed),
          Snackbar.LENGTH_LONG).show();
    }
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
  }

  @Override public void onBookSelected(String bookKey) {
    Intent bookIntent = new Intent(this, BookActivity.class);
    bookIntent.putExtra(Constants.EXTRA_KEY, bookKey);
    startActivity(bookIntent);
  }

  @Override public void onBackPressed() {
    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    if (currentFragment instanceof BookCreateFragment || currentFragment instanceof StashFragment) {
      toolbar.setTitle(R.string.app_name);
    }
    super.onBackPressed();
  }

  @Override public void onBookReleased(String bookKey) {
    getSupportFragmentManager().popBackStack();
    onBookSelected(bookKey);
  }

  @Override public void onBookAdd() {
    toolbar.setTitle(R.string.add_new_book_title);
    push(new BookCreateFragment());
  }

  public void navigateToMap() {
    startActivity(new Intent(this, MapActivity.class));
  }
}
