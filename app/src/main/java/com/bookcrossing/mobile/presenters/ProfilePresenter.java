package com.bookcrossing.mobile.presenters;

import android.net.Uri;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.profile.ProfileView;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by fobo66 on 08.05.17.
 */

@InjectViewState
public class ProfilePresenter extends BasePresenter<ProfileView> {

    public DatabaseReference getAcquiredBooks() {
        return acquiredBooks();
    }

    public Uri getPhotoUrl() {
        return firebaseWrapper.getAuth().getCurrentUser().getPhotoUrl();
    }
}