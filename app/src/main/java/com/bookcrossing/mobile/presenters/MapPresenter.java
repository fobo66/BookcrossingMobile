package com.bookcrossing.mobile.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.bookcrossing.mobile.models.Book;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.ui.map.MvpMapView;
import com.kelvinapps.rxfirebase.DataSnapshotMapper;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

import rx.functions.Action1;
import rx.functions.Func2;


@InjectViewState
public class MapPresenter extends BasePresenter<MvpMapView> {


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
                        Map<String, Coordinates> coordinatesWithBookTitlesMap = new LinkedHashMap<>();
                        for (String key :
                                places.keySet()) {
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
}
