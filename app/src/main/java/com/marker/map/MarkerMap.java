package com.marker.map;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Vibrator;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.destino.Destino;
import com.marker.destino.lugar.Lugar;
import com.marker.locator.LatLong;


public class MarkerMap implements OnMapLongClickListener, OnMapClickListener {
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String ANDROID_BUILDING_ID = "1";

    private Context context;
    private GoogleMap map;
    private Marker marker;
    private Marker userMarker;
    private Circle circle;
    private Location userLocation;
    private Lugar lugar;
    private float radio = 200.0f;
    // Geofence
    private Geofence geoFence;
    private GeoFenceHandler geoFenceHandler;

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
                this.radio,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        geoFence = sFence.toGeofence();
        this.addFence(sFence);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        confirmClick(point);
    }

    @Override
    public void onMapClick(LatLng point) {
        confirmClick(point);
    }

    private void confirmClick(LatLng point) {
        setPosition(point);
        MainActivity mActivity = (MainActivity) context;
        Vibrator vibe = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(50);
        mActivity.enableTrackButton(true);
    }

    public void setMap(GoogleMap map){
        this.map = map;
        this.map.setOnMapClickListener(this);
        this.map.setOnMapLongClickListener(this);
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
        LatLong posicion = LatLong.of(marker.getPosition());
        String nombre = String.format("%f, %f", posicion.latitude, posicion.longitude);
        this.setLugar(new Lugar(nombre, "", posicion));
    }

    public Location getLocation(){
        return this.userLocation;
    }

    public void setLocation(Location location){
        this.userLocation = location;
        LatLng latLng = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());

        if(userMarker == null){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker);

            userMarker = map.addMarker(new MarkerOptions().position(latLng)
                            .title("Location")
                            .icon(icon));
        } else {
            userMarker.setPosition(latLng);
        }
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
            circle.setRadius(fence.getRadius());
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

    public float getRadio() {
        return radio;
    }

    public void setRadio(float radio) {
        this.radio = radio;
    }

    public boolean markerPlacedOn(Destino destino) {
        return destino != null && getLugar().posicion.isEquivalentTo(destino.posicion);
    }
}
