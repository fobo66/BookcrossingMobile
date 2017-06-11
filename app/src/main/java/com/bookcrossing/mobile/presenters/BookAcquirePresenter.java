package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.acquire.BookAcquireView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import rx.functions.Action1;
import rx.functions.Func1;

@InjectViewState
public class BookAcquirePresenter extends BasePresenter<BookAcquireView> {

    public void setKeyExists(boolean keyExists) {
        this.keyExists = keyExists;
    }

    private boolean keyExists;

    public void handleAcquisition(final String key) {
        books().child(key).child("free").setValue(false);

        unsubscribeOnDestroy(RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class)
                .map(new Func1<Book, Book>() {
                    @Override
                    public Book call(Book book) {
                        acquiredBooks().child(key).setValue(book);
                        return book;
                    }
                })
                .subscribe(new Action1<Book>() {
                    @Override
                    public void call(Book book) {
                        getViewState().onAcquired();
                    }
                }));
    }

    // TODO: refactor this for RX after 1to2 migration
    public boolean isKeyValid(final String key) {
        books().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(key)) {
                    setKeyExists(true);
                } else {
                    setKeyExists(false);
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
