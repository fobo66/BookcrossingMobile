package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.main.MainView;
import com.google.firebase.database.DatabaseReference;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 21.12.2016.
 */

@InjectViewState
public class MainPresenter extends BasePresenter<MainView> {

    public DatabaseReference getBooks() {
        return getBooksReference();
    }
}
