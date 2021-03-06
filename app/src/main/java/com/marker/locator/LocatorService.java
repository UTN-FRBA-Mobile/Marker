package com.marker.locator;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.marker.app.GestorSesion;

public class LocatorService extends IntentService {
    private static final String TAG = "Locator Service";

    public LocatorService() {
        super(LocatorService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        final Context context = this;
        Locator locator = new Locator(context);
        // Pido la ubicacion
        locator.setClient(LocationServices.getFusedLocationProviderClient(context));
        locator.getLocation(new Locator.ResultadoListener(){

            @Override
            public void onResultado(LatLng latLng) {
                Log.i(TAG, "Solicitud de ubicacion: "+latLng.longitude+":"+latLng.latitude);
                storeLocation(latLng);
            }
        });

        // Genero una alarma que va a llamar a este mismo servicio en un minuto
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(LocatorService.this, LocatorService.class);
        PendingIntent pendingIntent = PendingIntent.getService(LocatorService.this, 1, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
    }

    private void storeLocation(LatLng latLng){
        SharedPreferences mPrefs = getSharedPreferences("marker", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = new Gson().toJson(latLng);
        prefsEditor.putString("location", json);
        prefsEditor.commit();
    }
}
