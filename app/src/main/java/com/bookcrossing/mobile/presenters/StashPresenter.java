package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.stash.StashView;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by fobo66 on 18.05.17.
 */

@InjectViewState
public class StashPresenter extends BasePresenter<StashView> {

    public DatabaseReference getStashedBooks() {
        return stash();
    }
}
