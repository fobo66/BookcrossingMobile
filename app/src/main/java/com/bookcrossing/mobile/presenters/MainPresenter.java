package com.bookcrossing.mobile.presenters;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.modules.App;
import com.bookcrossing.mobile.ui.main.MainView;
import com.bookcrossing.mobile.util.Constants;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.database.DatabaseReference;
import javax.inject.Inject;

/**
 * (c) 2016 Andrey Mukamolov aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 21.12.2016.
 */

@InjectViewState public class MainPresenter extends BasePresenter<MainView> {

  @Inject SharedPreferences preferences;

  public MainPresenter() {
    App.getComponent().inject(this);
  }

  public DatabaseReference getBooks() {
    return books();
  }

  public void checkForConsent(AdRequest.Builder adBuilder) {
    ConsentStatus consentStatus = loadConsentStatus();

    if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
      Bundle extras = new Bundle();
      extras.putString("npa", "1");

      adBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
    }
  }

  @NonNull private ConsentStatus loadConsentStatus() {
    return ConsentStatus.valueOf(preferences.getString(Constants.KEY_CONSENT_STATUS, "UNKNOWN"));
  }
}
