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
import androidx.annotation.NonNull;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.map.MvpMapView;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Maybe;
import io.reactivex.Single;
import moxy.InjectViewState;
import timber.log.Timber;

/**
 * Presenter for map screen
 */
@InjectViewState public class MapPresenter extends BasePresenter<MvpMapView> {
  public void getBooksPositions() {
    unsubscribeOnDestroy(
      RxFirebaseDatabase.observeValueEvent(places(), DataSnapshotMapper.mapOf(Coordinates.class))
        .flatMapIterable(placesMap -> placesMap.entrySet())
        .subscribe(place -> getViewState().onBookMarkerLoaded(place.getKey(), place.getValue()),
          throwable -> {
            Timber.e(throwable, "Failed to load marker");
            getViewState().onErrorToLoadMarker();
          }));
  }

  @NonNull public Maybe<Book> loadBookDetails(@NonNull String key) {
    return RxFirebaseDatabase.observeSingleValueEvent(books().child(key), Book.class);
  }

  @NonNull public Single<Location> requestUserLocation() {
    return getSystemServicesWrapper().getLocationRepository().getLastKnownUserLocation();
  }
}
