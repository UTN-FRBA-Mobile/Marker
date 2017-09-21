package com.marker.map;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MarkerMap {
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String ANDROID_BUILDING_ID = "1";
    public static final float ANDROID_BUILDING_RADIUS_METERS = 200.0f; // A sacar de las settings

    private GoogleMap map;
    private Marker marker;
    private Circle circle;
    private Location userLocation;
    private SimpleGeoFence geoFence;

    public MarkerMap(Context context){
    }

    public void createGeofences(LatLng position) {
        // Create internal "flattened" objects containing the geofence data.
        SimpleGeoFence mAndroidBuildingGeofence = new SimpleGeoFence(
                ANDROID_BUILDING_ID,
                position.latitude,
                position.longitude,
                ANDROID_BUILDING_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        geoFence = mAndroidBuildingGeofence;
        this.addFence(geoFence);
    }

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
        createGeofences(position);
    }

    public void addFence(SimpleGeoFence fence){
        circle = map.addCircle(new CircleOptions().center( new LatLng(fence.getLatitude(), fence.getLongitude()) )
                .radius( fence.getRadius() )
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2));
    }

    public void updateCamera(){
        map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));
    }


}
