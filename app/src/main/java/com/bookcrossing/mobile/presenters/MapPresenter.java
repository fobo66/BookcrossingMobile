package com.bookcrossing.mobile.presenters;

import android.location.Location;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.map.MvpMapView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Single;

@InjectViewState public class MapPresenter extends BasePresenter<MvpMapView> {

  private Map<String, Book> bookMap;
  private Map<Coordinates, String> coordinatesMap;

  public MapPresenter() {
    super();
  }

  public void getBooksPositions() {
    unsubscribeOnDestroy(
        RxFirebaseDatabase.observeValueEvent(places(), DataSnapshotMapper.mapOf(Coordinates.class))
            .zipWith(
                RxFirebaseDatabase.observeValueEvent(books(), DataSnapshotMapper.mapOf(Book.class)),
                (places, books) -> {
                  bookMap = books;
                  coordinatesMap = new HashMap<>();
                  Map<String, Coordinates> coordinatesWithBookTitlesMap = new LinkedHashMap<>();
                  for (String key : places.keySet()) {
                    coordinatesMap.put(places.get(key), key);
                    coordinatesWithBookTitlesMap.put(books.get(key).getName(), places.get(key));
                  }
                  return coordinatesWithBookTitlesMap;
                }).subscribe(places -> {
          for (String key : places.keySet()) {
            getViewState().onBookMarkerLoaded(key, places.get(key));
              }
        }, throwable -> {
          Crashlytics.logException(throwable);
          getViewState().onErrorToLoadMarker();
            }));
  }

  private String getKey(Coordinates coordinates) {
    return coordinatesMap.get(coordinates);
  }

  public String getKey(LatLng coordinates) {
    return coordinatesMap.get(new Coordinates(coordinates));
  }

  public String getSnippet(Coordinates coordinates) {
    return bookMap.get(getKey(coordinates)).getDescription();
  }

    public Single<Location> requestUserLocation() {
        return systemServicesWrapper.getLocationRepository().getLastKnownUserLocation();
  }
}
