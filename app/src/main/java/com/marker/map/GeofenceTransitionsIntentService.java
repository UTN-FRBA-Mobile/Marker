package com.marker.map;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.marker.app.GestorSesion;
import com.marker.firebase.Mensaje;


public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Geofence Service";

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
                Log.e(TAG, "Location Services info: Transition enter");
                //FIXME: deberia tomar datos del usuario y enviar la notificacion a quienes compartio el marker
                GestorSesion gestorSesion = GestorSesion.getInstancia();
                Mensaje fcm = Mensaje.newNotification();
                fcm.setTitle("Marker");
                fcm.setBody("Has llegado a destino");
                gestorSesion.getEmisorMensajes()
                        .enviar(gestorSesion.getUsuarioLoggeado(), fcm);
                String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0)
                        .getRequestId();

            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                Log.e(TAG, "Location Services info: Transition exit");
            } else if (Geofence.GEOFENCE_TRANSITION_DWELL == transitionType) {
                Log.e(TAG, "Location Services info: Transition dwell");
            }
        }
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