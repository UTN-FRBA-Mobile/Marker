package com.marker.locator;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.map.MarkerMap;

public class Locator {
    private FusedLocationProviderClient fusedClient;
    private MainActivity activity;
    private LocationManager manager;

    public Locator(Context context){
        this.manager = (LocationManager) context
                .getSystemService( Context.LOCATION_SERVICE );
        setClient(LocationServices.getFusedLocationProviderClient(context));
    }

    public void setClient(FusedLocationProviderClient client){
        this.fusedClient = client;
    }
    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    public Task<Location> getLastLocation() {
        return fusedClient.getLastLocation();
    }

    public void getLocation(final ResultadoListener listener) {
        getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location result = task.getResult();
                if (task.isSuccessful() && result != null) {
                    LatLng latLng = new LatLng(result.getLatitude(),
                            result.getLongitude());
                    listener.onResultado(latLng);
                }
            }
        });
    }

    public interface ResultadoListener {
        void onResultado(LatLng latLng);
    }
}
