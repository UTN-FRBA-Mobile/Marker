package com.marker.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.app.Marcador;
import com.marker.app.MarcadorManager;
import com.marker.app.MyApplication;
import com.marker.locator.Locator;

import java.text.MessageFormat;
import java.util.Map;

public class ServicioMensajeria extends FirebaseMessagingService {

    private static final String TAG = ServicioMensajeria.class.getSimpleName();
    public static final String CH_DEFAULT = "channel_00";
    public static final String CH_MARKERS = "channel_01";
    public static final String CH_LLEGADAS = "channel_02";

    /**Called when message is received.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "FCM Recibido From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.isEmpty()) {
            return;
        }
        Log.d(TAG, "FCM data");
        String esData = data.get("esData");
        if (esData != null) {
            if (Boolean.valueOf(esData)) {
                onDataPayload(data);
            } else {
                notificarData(data);
            }
        } else {
            onDataPayload(data);
        }
    }

    private void onDataPayload(Map<String, String> data) {
        final Mensaje fcm = Mensaje.newDataMessage(data);
        final GestorSesion gestorSesion = GestorSesion.getInstancia(this);
        Mensaje.TipoData tipoData = fcm.getTipoData();
        Log.d(TAG, "Protocolo: " + tipoData);
        if (tipoData == null) {
            return;
        }
        switch (tipoData) {
            case MARKER:
                Log.d(TAG, "Guardando nuevo marker");
                Marcador marker = fcm.getMarker();
                MarcadorManager.getInstancia(this).agregarMarker(marker);
                Intent intent = new Intent(getString(R.string.BROADCAST_MARKER));
                intent.putExtra(getString(R.string.BROADCAST_ACTION),
                        R.string.BROADCAST_ACTION_NEW_MARKER);
                sendBroadcast(intent);
                notificarMarker(marker);
                break;
            case PEDIDO_POSICION:
                Log.d(TAG, "Pidiendo posicion");
                Locator locator = new Locator(this);
                locator.getLocation(new Locator.ResultadoListener() {
                        @Override
                        public void onResultado(LatLng latLng) {
                            Log.d(TAG, "Enviando posicion");
                            Mensaje mensaje = Mensaje.newDataMessage();
                            mensaje.setTipoData(Mensaje.TipoData.POSICION);
                            mensaje.getPayload().put("posicion", new Gson().toJson(latLng));
                            String idEmisor = fcm.getPayload().get("idEmisor");
                            new EmisorMensajes().enviar(gestorSesion.getUsuarioLoggeado().getId(), idEmisor, mensaje);
                        }
                    });
                break;
            case POSICION:
                Log.d(TAG, "Recibiendo posicion");
                String gsonPosicion = fcm.getPayload().get("posicion");
                LatLng posicion = new Gson()
                        .fromJson(gsonPosicion, LatLng.class);
                String uid = fcm.getPayload().get("idEmisor");
                Intent intentPos = new Intent(getString(R.string.BROADCAST_MARKER));
                intentPos.putExtra("posicion", posicion);
                intentPos.putExtra("usuario", uid);
                intentPos.putExtra(getString(R.string.BROADCAST_ACTION),
                        R.string.BROADCAST_ACTION_POSITION);
                sendBroadcast(intentPos);
                break;
            default:
                Log.d(TAG, "Protocolo desconocido");
                break;
        }
    }

    private void notificarMarker(Marcador marker) {
        if (MyApplication.isActivityVisible()) {
            alert();
            return;
        }
        NotificationManager nManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        String texto = MessageFormat.format("Nuevo marker de {0}!",
                marker.getUser().getName());

        for (StatusBarNotification sbn : nManager.getActiveNotifications()) {
            if (CH_MARKERS.equals(sbn.getTag())) {
                Bundle extras = sbn.getNotification().extras;
                texto = extras.getString(Notification.EXTRA_TEXT) +
                        "\n" + texto;
                break;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(MainActivity.ACCION_MOSTRAR_MARKERS_AL_INICIAR, true);
        notificar(CH_MARKERS, texto, "Nuevos markers!", bundle);
    }

    private void notificarData(Map<String, String> data) {
        Mensaje fcm = Mensaje.newDataMessage(data);
        String titulo = fcm.getTitle();
        String cuerpo = fcm.getBody();
        String channel = data.get("channel");
        titulo = titulo != null ? titulo : "";
        cuerpo = cuerpo != null ? cuerpo : "";
        channel = channel != null ? channel : CH_DEFAULT;
        notificar(channel, cuerpo, titulo);
    }

    private void notificar(String channel, String texto, String titulo) {
        notificar(channel, texto, titulo, null);
    }

    private void notificar(String channel, String texto, String titulo, Bundle bundle) {
        Bitmap icono = BitmapFactory
                .decodeResource(getResources(), R.mipmap.ic_launcher_round);
        Intent intent = new Intent(this, MainActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.mipmap.ic_launcher_tray)
                .setLargeIcon(icono)
                .setContentTitle(titulo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentText(texto)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent);
        NotificationManager nManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(channel, 0, notificationBuilder.build());
        alert();
    }

    private void alert(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Uri notif = Uri.parse(sharedPreferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND"));
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notif);
        if (ringtone == null){
            notif = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notif);
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .build();
            ringtone.setAudioAttributes(attr);
        }
        ringtone.play();
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);
    }
}