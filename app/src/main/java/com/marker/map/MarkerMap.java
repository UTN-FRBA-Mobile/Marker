package com.marker.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerMap {
    private GoogleMap map;
    private Marker marker;

    public MarkerMap(){}

    public void setMap(GoogleMap map){
        this.map = map;
    }

    public void setPosition(LatLng position){
        if(marker == null){
            addMarker(position);
        } else {
            marker.setPosition(position);
        }
    }

    public void addMarker(LatLng position){
        marker = map.addMarker(new MarkerOptions().position(position).title("Marker"));
    }

    public void updateCamera(){
        map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));
    }


}
