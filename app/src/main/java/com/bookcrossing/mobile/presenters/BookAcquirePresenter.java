package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.ui.acquire.BookAcquireView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

@InjectViewState
public class BookAcquirePresenter extends BasePresenter<BookAcquireView> {

    private boolean keyExists;

    public void handleAcquisition(String key) {
        getBooksReference().child(key).child("free").setValue(false);
    }

    public boolean isKeyValid(final String key) {
        keyExists = false;
        getBooksReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(key)) {
                    keyExists = true;
                } else {
                    getViewState().onIncorrectKey();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return keyExists;
    }
}
