package com.bookcrossing.mobile.backend;

import com.google.appengine.repackaged.com.google.type.LatLng;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * The object model for the data we are sending through endpoints
 */

@Entity
public class Book {
    @Id Long id;
    String author;
    String name;
    boolean free;
    LatLng position;
    List<LatLng> placesHistory;

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public boolean isFree() {
        return free;
    }

    public String getPosition() {
        return "" + position.getLatitude() + ";" + position.getLongitude();
    }
/*
    public List<LatLng> getPlacesHistory() {
        return placesHistory;
    }*/
}