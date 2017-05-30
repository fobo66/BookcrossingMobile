package com.bookcrossing.mobile.ui.map;

import android.Manifest;
import android.os.Bundle;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.presenters.MapPresenter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MapActivity extends MvpAppCompatActivity implements MvpMapView, OnMapReadyCallback {

    @InjectPresenter
    MapPresenter presenter;

    private GoogleMap map;
    private RxPermissions permissions;
    private Disposable locationDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        permissions = new RxPermissions(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationDisposable.dispose();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        locationDisposable = requestLocationPermission()
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean granted) throws Exception {
                        if (granted) {
                            map.setMyLocationEnabled(true);
                        }
                    }
                });
        presenter.getBooksPositions();
    }

    public Observable<Boolean> requestLocationPermission() {
        return permissions.request(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void setBookMarker(String title, Coordinates coordinates) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(coordinates.lat, coordinates.lng))
                .title(title)
        );
    }
}
