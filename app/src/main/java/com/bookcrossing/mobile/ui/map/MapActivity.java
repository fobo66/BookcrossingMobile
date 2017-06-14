package com.bookcrossing.mobile.ui.map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.bookcrossing.mobile.R;
import com.bookcrossing.mobile.models.Coordinates;
import com.bookcrossing.mobile.presenters.MapPresenter;
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
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MapActivity extends MvpAppCompatActivity
    implements MvpMapView, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

  public static final float DEFAULT_ZOOM_LEVEL = 16.0f;

  @InjectPresenter MapPresenter presenter;

  private GoogleMap map;
  private RxPermissions permissions;
  private Disposable locationDisposable;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    permissions = new RxPermissions(this);
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    locationDisposable.dispose();
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    locationDisposable = requestLocationPermission().subscribe(new Consumer<Boolean>() {
      @Override public void accept(@NonNull Boolean granted) throws Exception {
        if (granted) {
          map.setMyLocationEnabled(true);
        }
      }
    });
    map.setOnInfoWindowClickListener(this);
    presenter.getBooksPositions();

    if (getIntent() != null) {
      Coordinates requestedZoomPosition =
          getIntent().getParcelableExtra(Constants.EXTRA_COORDINATES);
      if (requestedZoomPosition != null) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(requestedZoomPosition.lat, requestedZoomPosition.lng), DEFAULT_ZOOM_LEVEL));
      }
    }
  }

  public Observable<Boolean> requestLocationPermission() {
    return permissions.request(Manifest.permission.ACCESS_FINE_LOCATION);
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

  @Override public void onInfoWindowClick(Marker marker) {
    Intent intent = new Intent(this, BookActivity.class);
    intent.putExtra(Constants.EXTRA_KEY, presenter.getKey(marker.getPosition()));
    startActivity(intent);
  }
}
