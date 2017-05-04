package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.ui.bookpreview.BookView;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import rx.Subscription;
import rx.functions.Action1;

@InjectViewState
public class BookPresenter extends BasePresenter<BookView> {

    public void subscribeToBookReference(String key) {
        Subscription bookSubscription = RxFirebaseDatabase.observeSingleValueEvent(
                getBooksReference().child(key), Book.class)
                .subscribe(new Action1<Book>() {
                    @Override
                    public void call(Book book) {
                        getViewState().onBookLoaded(book);
                    }
                });
        unsubscribeOnDestroy(bookSubscription);
    }

    public void handleBookStashing() {

    }
}
