package com.bookcrossing.mobile.presenters;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.modules.App;
import com.bookcrossing.mobile.util.Constants;
import com.bookcrossing.mobile.util.FirebaseWrapper;
import com.bookcrossing.mobile.util.SystemServicesWrapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.rx.ObservableFactory;
import io.reactivex.Observable;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 29.12.2016.
 */

public class BasePresenter<View extends MvpView> extends MvpPresenter<View> {
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    protected String city = "undefined";
    protected FirebaseWrapper firebaseWrapper;
    protected SystemServicesWrapper systemServicesWrapper;

    public BasePresenter() {
        firebaseWrapper = new FirebaseWrapper();
        systemServicesWrapper = new SystemServicesWrapper();
        App.getComponent().inject(firebaseWrapper);
        App.getComponent().inject(systemServicesWrapper);
    }

    protected void unsubscribeOnDestroy(@NonNull Subscription subscription) {
        compositeSubscription.add(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    protected DatabaseReference books() {
        return firebaseWrapper.getDatabase().getReference("books");
    }

    protected DatabaseReference stash() {
        return firebaseWrapper.getDatabase().getReference("stash").child(getUserId());
    }

    protected DatabaseReference acquiredBooks() {
        return firebaseWrapper.getDatabase().getReference("acquiredBooks").child(getUserId());
    }

    protected DatabaseReference places() {
        return firebaseWrapper.getDatabase().getReference("places");
    }

    protected DatabaseReference placesHistory(String key) {
        return firebaseWrapper.getDatabase().getReference("placesHistory").child(key);
    }

    private String getUserId() {
        if (firebaseWrapper.getAuth().getCurrentUser() != null) {
            return firebaseWrapper.getAuth().getCurrentUser().getUid();
        }

        return Constants.DEFAULT_USER;
    }

    public StorageReference resolveCover(String key) {
         return firebaseWrapper.getStorage().getReference(key + ".jpg");
    }

    public Uri buildBookUri(String key) {
        return new Uri.Builder()
                .scheme("bookcrossing")
                .authority(Constants.PACKAGE_NAME)
                .path("book")
                .appendQueryParameter(Constants.EXTRA_KEY, key)
                .build();
    }

    protected String getCity() {
        return systemServicesWrapper.getPreferences().getString(Constants.EXTRA_CITY, getDefaultCity());
    }

    private String getDefaultCity() {
        return systemServicesWrapper.getPreferences().getString(Constants.EXTRA_DEFAULT_CITY, systemServicesWrapper.getApp().getString(R.string.default_city));
    }

    public Observable<List<Address>> resolveUserCity() {
        return ObservableFactory.from(SmartLocation.with(systemServicesWrapper.getApp().getApplicationContext()).location().oneFix())
                .flatMapSingle(new Function<Location, SingleSource<List<Address>>>() {
                    @Override
                    public SingleSource<List<Address>> apply(@io.reactivex.annotations.NonNull Location location) throws Exception {
                        return ObservableFactory.fromLocation(systemServicesWrapper.getApp().getApplicationContext(), location, 1);
                    }
                });
    }

    public void saveCity(@io.reactivex.annotations.NonNull List<Address> addresses) {
        city = addresses.get(0).getLocality();
        SharedPreferences.Editor editor = systemServicesWrapper.getPreferences().edit();
        editor.putString(Constants.EXTRA_CITY, city);
        editor.putString(Constants.EXTRA_DEFAULT_CITY, city);
        editor.apply();
    }
}
