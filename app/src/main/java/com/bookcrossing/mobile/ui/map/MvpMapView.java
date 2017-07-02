package com.bookcrossing.mobile.ui.map;

import com.arellomobile.mvp.MvpView;
import com.bookcrossing.mobile.models.Coordinates;
import com.google.android.gms.maps.model.LatLng;

public interface MvpMapView extends MvpView {
  void onBookMarkerLoaded(String key, Coordinates coordinates);

  void onErrorToLoadMarker();

  void onUserLocationReceived(LatLng coordinates);
}
