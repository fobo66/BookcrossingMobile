package com.bookcrossing.mobile.presenters;

import android.content.SharedPreferences;
import android.location.Address;
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

import io.nlopez.smartlocation.rx.ObservableFactory;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 29.12.2016.
 */

public class BasePresenter<V extends MvpView> extends MvpPresenter<V> {
  private CompositeDisposable compositeSubscription = new CompositeDisposable();
  protected String city = "undefined";
  protected FirebaseWrapper firebaseWrapper;
  protected SystemServicesWrapper systemServicesWrapper;

  public BasePresenter() {
    firebaseWrapper = new FirebaseWrapper();
    systemServicesWrapper = new SystemServicesWrapper();
    App.getComponent().inject(firebaseWrapper);
    App.getComponent().inject(systemServicesWrapper);
  }

  protected void unsubscribeOnDestroy(@NonNull Disposable subscription) {
    compositeSubscription.add(subscription);
  }

  @Override public void onDestroy() {
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
    if (isAuthenticated()) {
      return firebaseWrapper.getAuth().getCurrentUser().getUid();
    }

    return Constants.DEFAULT_USER;
  }

  public StorageReference resolveCover(String key) {
    return firebaseWrapper.getStorage().getReference(key + ".jpg");
  }

  public Uri buildBookUri(String key) {
    return new Uri.Builder().scheme("bookcrossing")
        .authority(Constants.PACKAGE_NAME)
        .path("book")
        .appendQueryParameter(Constants.EXTRA_KEY, key)
        .build();
  }

  protected String getCity() {
    return systemServicesWrapper.getPreferences().getString(Constants.EXTRA_CITY, getDefaultCity());
  }

  public String getDefaultCity() {
    return systemServicesWrapper.getPreferences()
        .getString(Constants.EXTRA_DEFAULT_CITY,
            systemServicesWrapper.getApp().getString(R.string.default_city));
  }

  public Observable<List<Address>> resolveUserCity() {
    return ObservableFactory.from(systemServicesWrapper.getLocation().location().oneFix())
        .flatMapSingle(location -> ObservableFactory.fromLocation(
            systemServicesWrapper.getApp().getApplicationContext(), location, 1));
  }

    public void saveCity(@NonNull List<Address> addresses) {
    if (!addresses.isEmpty()) {
      city = addresses.get(0).getLocality();
      saveCity(city);
    }
  }

  public void saveCity(String city) {
    SharedPreferences.Editor editor = systemServicesWrapper.getPreferences().edit();
    editor.putString(Constants.EXTRA_CITY, city);
    editor.putString(Constants.EXTRA_DEFAULT_CITY, city);
    editor.apply();
  }

  public boolean isAuthenticated() {
    return firebaseWrapper.getAuth().getCurrentUser() != null;
  }
}
