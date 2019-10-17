/*
 *    Copyright 2019 Andrey Mukamolov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.presenters;

import android.location.Location;

import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.map.MvpMapView;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Single;
import moxy.InjectViewState;
import timber.log.Timber;

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
          Timber.e(throwable, "Failed to load marker");
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
