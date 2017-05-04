package com.bookcrossing.mobile.ui.create;

import com.arellomobile.mvp.MvpView;

public interface BookCreateView extends MvpView {
    void OnUpload();
    void OnNameChange();
    void OnReleased(String newKey);
}
