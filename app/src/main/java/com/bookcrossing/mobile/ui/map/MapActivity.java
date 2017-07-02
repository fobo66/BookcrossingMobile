package com.bookcrossing.mobile.ui.map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.presenters.MapPresenter;
import com.bookcrossing.mobile.ui.base.BaseActivity;
import com.bookcrossing.mobile.ui.bookpreview.BookActivity;
import com.bookcrossing.mobile.util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MapActivity extends BaseActivity
    implements MvpMapView, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

  public static final float DEFAULT_ZOOM_LEVEL = 16.0f;

  @InjectPresenter MapPresenter presenter;

  private GoogleMap map;
  private RxPermissions permissions;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    permissions = new RxPermissions(this);
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    subscriptions.add(requestLocationPermission().subscribe(new Consumer<Boolean>() {
      @Override public void accept(@NonNull Boolean granted) throws Exception {
        if (granted) {
          map.setMyLocationEnabled(true);
        }
      }
    }));
    map.setOnInfoWindowClickListener(this);
    presenter.getBooksPositions();

    if (getIntent() != null) {
      Coordinates requestedZoomPosition =
          getIntent().getParcelableExtra(Constants.EXTRA_COORDINATES);
      if (requestedZoomPosition != null) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(requestedZoomPosition.lat, requestedZoomPosition.lng), DEFAULT_ZOOM_LEVEL));
      } else {
        requestUserLocation();
      }
    } else {
      requestUserLocation();
    }
  }

  private void requestUserLocation() {
    subscriptions.add(
        requestLocationPermission().flatMap(new Function<Boolean, ObservableSource<Location>>() {
          @Override public ObservableSource<Location> apply(@NonNull Boolean granted)
              throws Exception {
            if (granted) {
              return presenter.requestUserLocation();
            } else {
              return Observable.empty();
            }
          }
        }).subscribe(new Consumer<Location>() {
          @Override public void accept(@NonNull Location location) throws Exception {
            onUserLocationReceived(new LatLng(location.getLatitude(), location.getLongitude()));
          }
        }));
  }

  public Observable<Boolean> requestLocationPermission() {
    return permissions.request(Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION);
  }

  @Override public void onBookMarkerLoaded(String title, Coordinates coordinates) {
    map.addMarker(new MarkerOptions().position(new LatLng(coordinates.lat, coordinates.lng))
        .title(title)
        .snippet(presenter.getSnippet(coordinates)));
  }

  @Override public void onErrorToLoadMarker() {
    new AlertDialog.Builder(this).setMessage(R.string.failed_to_load_books_message)
        .setTitle(R.string.error_dialog_title)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
          }
        })
        .show();
  }

  @Override public void onUserLocationReceived(LatLng coordinates) {
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, DEFAULT_ZOOM_LEVEL));
  }

  @Override public void onInfoWindowClick(Marker marker) {
    Intent intent = new Intent(this, BookActivity.class);
    intent.putExtra(Constants.EXTRA_KEY, presenter.getKey(marker.getPosition()));
    startActivity(intent);
  }
}
