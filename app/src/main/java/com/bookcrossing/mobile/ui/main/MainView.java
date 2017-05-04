package com.bookcrossing.mobile.ui.main;

import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.models.Book;

import java.util.List;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 21.12.2016.
 */

public interface MainView extends MvpView {
    void onItems(List<Book> items);
}
