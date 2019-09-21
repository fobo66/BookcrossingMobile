package com.bookcrossing.mobile.presenters;

import com.bookcrossing.mobile.ui.stash.StashView;
import com.google.firebase.database.DatabaseReference;

import moxy.InjectViewState;

@InjectViewState public class StashPresenter extends BasePresenter<StashView> {

  public DatabaseReference getStashedBooks() {
    return stash();
  }
}
