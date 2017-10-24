package com.marker.locator;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.marker.app.GestorSesion;

public class LocatorService extends IntentService {
    private static final String TAG = "Locator Service";
    private Handler handler = new Handler();
    private Runnable runnable;

    public LocatorService() {
        super(LocatorService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = this;
        Locator locator = new Locator(context);
        // Pido la ubicacion
        locator.setClient(LocationServices.getFusedLocationProviderClient(context));
        locator.getLastLocation();

        Log.i(TAG, "Solicitud de ubicacion");
     
        // Genero una alarma que va a llamar a este mismo servicio en un minuto
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(LocatorService.this, LocatorService.class);
        PendingIntent pendingIntent = PendingIntent.getService(LocatorService.this, 1, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60000, pendingIntent);
    }
}
