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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.map.MarkerMap;

public class Locator {
    private FusedLocationProviderClient fusedClient;
    private MainActivity activity;
    private Context context;
    private LocationManager manager;

    static final int GPS_ENABLE_REQUEST = 40;

    public Locator(MainActivity activity){
        this.activity = activity;
        this.manager = (LocationManager) activity.getSystemService( Context.LOCATION_SERVICE );
    }

    public Locator(Context context){
        this.context = context;
        this.manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
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
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showGPSDiabledDialog();
        }
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

    public void showGPSDiabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("GPS Deshabilitado");
        builder.setMessage("El GPS no esta encendido, por lo que no podra utilizar todas las funcionalidades de la aplicacion");
        builder.setPositiveButton("Activar GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
            }
        }).setNegativeButton("No, solo salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
}
