package com.example.tinder.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class UserLocation {
    private GeoPoint geoPoint;
    private @ServerTimestamp String timeStamp;

    public UserLocation(GeoPoint geoPoint, String timeStamp) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
    }

    public UserLocation() {
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
