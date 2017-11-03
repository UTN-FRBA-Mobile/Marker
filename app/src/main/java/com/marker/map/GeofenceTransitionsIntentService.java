package com.marker.map;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.facebook.User;
import com.marker.firebase.EmisorMensajes;
import com.marker.firebase.Mensaje;

import java.util.ArrayList;


public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Geofence Service";
    static final int GEO_FENCE_RESULT = 41;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        } else {
            int transitionType = geoFenceEvent.getGeofenceTransition();

            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                // Entra a la geofence
                Log.i(TAG, "Location Services info: Transition enter");

                String userId = intent.getStringExtra("userId");
                String userName = intent.getStringExtra("userName");
                Log.i(TAG, "Contact who shares: " + userId);
                sendNotification(userId, userId, "");

                ArrayList<String> contacts = intent.getStringArrayListExtra("contacts");
                for(String contact : contacts){
                    Log.i(TAG, "Contact to share: " + contact);
                    sendNotification(userId, contact, userName);
                }

                broadcastFinish();

            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                Log.i(TAG, "Location Services info: Transition exit");
            } else if (Geofence.GEOFENCE_TRANSITION_DWELL == transitionType) {
                Log.i(TAG, "Location Services info: Transition dwell");
            }
        }
    }

    private void broadcastFinish() {
        Intent broadcast = new Intent();
        broadcast.setAction(getString(R.string.BROADCAST_GEOFENCE));
        broadcast.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcast);
    }

    private void sendNotification(String from, String to, String fromName) {
        Mensaje fcm = Mensaje.newNotification();
        fcm.setTitle("Marker");
        if(fromName == ""){
            fcm.setBody("Has llegado a destino");
        } else {
            fcm.setBody(fromName + "ha llegado a destino");
        }
        (new EmisorMensajes()).enviar(from, to, fcm);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }
}