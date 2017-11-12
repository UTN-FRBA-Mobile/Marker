package com.marker.map;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.marker.MainActivity;
import com.marker.facebook.User;

import java.util.ArrayList;
import java.util.List;

public class GeoFenceHandler {
    private Geofence geoFence;
    private GeofencingClient geoFenceClient;
    private PendingIntent geofencePendingIntent;
    private Context context;
    private String userId;
    private String userName;

    private static final String TAG = "Geofence Handler";

    public GeoFenceHandler(Context context){
        this.context = context;
        geoFenceClient = new GeofencingClient(context);
    }

    public void setGeoFence(Geofence fence){
        this.geoFence = fence;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geoFence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(ArrayList<String> contactsToShare, String markerId) {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("markerId", markerId);
        intent.putStringArrayListExtra("contacts", contactsToShare);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public void activateFence(ArrayList<String> contactsToShare, String markerId){
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geoFenceClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(contactsToShare, markerId))
                .addOnSuccessListener((MainActivity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MainActivity mActivity = (MainActivity) context;
                        mActivity.showSnackbar("Agregada fence");
                        // ...
                    }
                })
                .addOnFailureListener((MainActivity)this.context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MainActivity mActivity = (MainActivity) context;
                        mActivity.showSnackbar("Error al agregar fence");
                        // ...
                    }
                });
    }

    public void desactivateFence(){
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(geoFence != null){
            List<String> fencesId = new ArrayList<>();
            fencesId.add(geoFence.getRequestId());
            geoFenceClient.removeGeofences(fencesId);
            geoFence = null;
        }
    }
}
