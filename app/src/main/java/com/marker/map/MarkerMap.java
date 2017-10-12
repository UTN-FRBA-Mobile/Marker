package com.marker.map;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.lugar.Lugar;


public class MarkerMap {
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String ANDROID_BUILDING_ID = "1";
    public static final float ANDROID_BUILDING_RADIUS_METERS = 200.0f; // A sacar de las settings

    private Context context;
    private GoogleMap map;
    private Marker marker;
    private Circle circle;
    private Location userLocation;
    private Geofence geoFence;
    private GeoFenceHandler geoFenceHandler;
    private Lugar lugar;

    public MarkerMap(Context context){
        this.context = context;
        this.geoFenceHandler = new GeoFenceHandler(context);
    }

    public void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
        SimpleGeoFence sFence = new SimpleGeoFence(
                ANDROID_BUILDING_ID,
                marker.getPosition().latitude,
                marker.getPosition().longitude,
                ANDROID_BUILDING_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        geoFence = sFence.toGeofence();
        this.addFence(sFence);
    }

    public void setMap(GoogleMap map){
        this.map = map;
        this.map.getUiSettings().setMapToolbarEnabled(false);
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setPosition(LatLng position){
        if(marker == null){
            addMarker(position);
        } else {
            marker.setPosition(position);
            createGeofences();
        }
    }

    public Location getLocation(){
        return this.userLocation;
    }

    public void setLocation(Location location){
        this.userLocation = location;
        LatLng latLng = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());


        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker);

        map.addMarker(new MarkerOptions().position(latLng)
                .title("Location")
                .icon(icon));
    }

    public void addMarker(LatLng position){
        marker = map.addMarker(new MarkerOptions().position(position).title("Marker"));
        createGeofences();
    }

    public void activateFence(){
        this.geoFenceHandler.setGeoFence(geoFence);
        this.geoFenceHandler.activateFence();
    }

    public void addFence(SimpleGeoFence fence) {
        if(circle == null){
            circle = map.addCircle(new CircleOptions().center( new LatLng(fence.getLatitude(), fence.getLongitude()) )
                    .radius( fence.getRadius() )
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2));
        } else {
            circle.setCenter(marker.getPosition());
        }

    }

    public void updateCamera(){
        map.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15.0f));
        circle.setCenter(marker.getPosition());
    }

    public void updateCameraOnLocation(){
        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15.0f));
    }

    public void centerCamera(){
        if(marker == null){
            updateCameraOnLocation();
        } else if(userLocation == null){
            updateCamera();
        } else if(marker != null && userLocation != null){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            builder.include(marker.getPosition());
            builder.include(userLatLng);
            LatLngBounds bounds = builder.build();
            int width = context.getResources().getDisplayMetrics().widthPixels;
            int height = context.getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.30); // offset from edges of the map 10% of screen
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
        } else {

        }
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    public Lugar getLugar() {
        return lugar;
    }
}
