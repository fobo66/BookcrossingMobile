package com.bookcrossing.mobile.util;

import android.util.SparseIntArray;

import androidx.annotation.IdRes;

import com.bookcrossing.mobile.R;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 23.06.17.
 *
 * Class for gracefully resolving NavigationDrawer item's click
 * In the future, there will be annotation processor for collecting our
 * SparseArray of fragments that are in the drawer
 */

public class NavigationDrawerResolver {
  private static final SparseIntArray navigationDrawerItems;

  static {
    navigationDrawerItems = new SparseIntArray();
    navigationDrawerItems.put(R.id.nav_catalogue, R.id.mainFragment);
    navigationDrawerItems.put(R.id.nav_stash, R.id.stashFragment);
    navigationDrawerItems.put(R.id.nav_books_map, R.id.mapActivity);
    navigationDrawerItems.put(R.id.nav_profile, R.id.profileFragment);
    navigationDrawerItems.put(R.id.nav_settings, R.id.settingsFragment);
  }

  /**
   * Return requested fragment by its id
   * @param itemId NavigationDrawer's MenuItem id
   * @return Fragment that should be inflated
   */
  @IdRes
  public static int resolveNavigationDrawerItem(@IdRes int itemId) {
    return navigationDrawerItems.get(itemId);
  }
}
