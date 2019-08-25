package com.bookcrossing.mobile.util;

import android.util.SparseArray;
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.ui.main.MainFragment;
import com.bookcrossing.mobile.ui.profile.ProfileFragment;
import com.bookcrossing.mobile.ui.settings.SettingsFragment;
import com.bookcrossing.mobile.ui.stash.StashFragment;
import com.crashlytics.android.Crashlytics;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 23.06.17.
 *
 * Class for gracefully resolving NavigationDrawer item's click
 * In the future, there will be annotation processor for collecting our
 * SparseArray of fragments that are in the drawer
 */

public class NavigationDrawerResolver {
  private static final SparseArray<Class<? extends Fragment>> navigationDrawerItems;

  static {
    navigationDrawerItems = new SparseArray<>();
    navigationDrawerItems.put(R.id.nav_catalogue, MainFragment.class);
    navigationDrawerItems.put(R.id.nav_stash, StashFragment.class);
    navigationDrawerItems.put(R.id.nav_profile, ProfileFragment.class);
    navigationDrawerItems.put(R.id.nav_settings, SettingsFragment.class);
  }

  /**
   * Return requested fragment by its id
   * @param itemId NavigationDrawer's MenuItem id
   * @return Fragment that should be inflated
   */
  public static Fragment resolveNavigationDrawerItem(@IdRes int itemId) {
    try {
      return navigationDrawerItems.get(itemId).newInstance();
    } catch (Exception e) {
      Crashlytics.logException(e);
      return new MainFragment();
    }
  }
}
