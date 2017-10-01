package com.marker.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.marker.MainActivity;
import com.marker.R;

public class Permission {
    MainActivity activity;

    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    public Permission(MainActivity activity){
        this.activity = activity;
    }

    public boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this.activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this.activity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {

            this.activity.showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activity.startLocationPermissionRequest();
                        }
                    });

        } else {
            this.activity.startLocationPermissionRequest();
        }
    }

    public void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this.activity,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}
