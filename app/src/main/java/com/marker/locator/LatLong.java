package com.marker.locator;

import com.google.android.gms.maps.model.LatLng;

public class LatLong {
    public Double latitude;
    public Double longitude;

    public LatLong() {}

    public LatLong(Double lat, Double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public static LatLong of(LatLng latLng){
        return new LatLong(latLng.latitude, latLng.longitude);
    }

    public static LatLng toLatLng(LatLong latLong){
        return new LatLng(latLong.latitude, latLong.longitude);
    }

    public boolean isEquivalentTo(LatLong position) {
        return this.latitude.equals(position.latitude) &&
                this.longitude.equals(position.longitude);
    }
}