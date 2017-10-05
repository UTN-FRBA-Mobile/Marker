package com.marker.locator;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marker.BuildConfig;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.map.MarkerMap;

public class Locator {
    private FusedLocationProviderClient fusedClient;
    private MainActivity activity;

    public Locator(MainActivity activity){
        this.activity = activity;
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

    public void getLocation() {
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            this.getLocationOnMap();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return activity.permission.checkPermissions();
    }

    private void requestPermissions() {
        activity.permission.requestPermissions();
    }

    public void getLocationOnMap(){
        this.getLastLocation()
                .addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            activity.map.setLocation(task.getResult());
                            activity.map.centerCamera();
                        } else {
                        }
                    }
                });
    }
}
