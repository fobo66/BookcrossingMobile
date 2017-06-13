package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.stash.StashView;
import com.google.firebase.database.DatabaseReference;

@InjectViewState public class StashPresenter extends BasePresenter<StashView> {

  public DatabaseReference getStashedBooks() {
    return stash();
  }
}
