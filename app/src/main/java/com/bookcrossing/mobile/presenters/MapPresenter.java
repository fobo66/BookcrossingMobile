package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.map.MvpMapView;
import com.google.android.gms.maps.model.LatLng;
import com.kelvinapps.rxfirebase.DataSnapshotMapper;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import rx.functions.Action1;
import rx.functions.Func2;


@InjectViewState
public class MapPresenter extends BasePresenter<MvpMapView> {

    private Map<String, Book> bookMap;
    private Map<Coordinates, String> coordinatesMap;


    public MapPresenter() {
        super();
    }

    public void getBooksPositions() {
        unsubscribeOnDestroy(RxFirebaseDatabase.observeValueEvent(places(), DataSnapshotMapper.mapOf(Coordinates.class))
                .zipWith(RxFirebaseDatabase.observeValueEvent(books(), DataSnapshotMapper.mapOf(Book.class)),
                        new Func2<LinkedHashMap<String,Coordinates>, LinkedHashMap<String,Book>, Map<String,Coordinates>>() {
                    @Override
                    public Map<String,Coordinates> call(LinkedHashMap<String, Coordinates> places,
                                                                  LinkedHashMap<String,Book> books) {
                        bookMap = books;
                        coordinatesMap = new HashMap<>();
                        Map<String, Coordinates> coordinatesWithBookTitlesMap = new LinkedHashMap<>();
                        for (String key : places.keySet()) {
                            coordinatesMap.put(places.get(key), key);
                            coordinatesWithBookTitlesMap.put(books.get(key).getName(), places.get(key));
                        }
                        return coordinatesWithBookTitlesMap;
                    }
                })
        .subscribe(new Action1<Map<String, Coordinates>>() {
            @Override
            public void call(Map<String, Coordinates> places) {
                for (String key : places.keySet()) {
                    getViewState().setBookMarker(key, places.get(key));
                }
            }
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
}
