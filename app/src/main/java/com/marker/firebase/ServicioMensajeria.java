package com.marker.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.marker.MainActivity;
import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.app.Marcador;
import com.marker.app.MarcadorManager;
import com.marker.locator.Locator;

import java.util.Map;

public class ServicioMensajeria extends FirebaseMessagingService {

    private static final String TAG = ServicioMensajeria.class.getSimpleName();

    /**Called when message is received.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "FCM Recibido From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.d(TAG, "FCM data");
            onDataPayload(data);
        }
        // Check if message contains a notification payload.
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            String notificationBody = notification.getBody();
            Log.d(TAG, "Message Notification Body: " + notificationBody);
            sendNotification(notification);
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

    /**Create and show a simple notification containing the received FCM message.
     * @param notification FCM message body received.
     */
    private void sendNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //Este codigo esta deprecado.. deberiamos usar la version stable
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
        alert();
    }

    private void alert(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Uri notif = Uri.parse(sharedPreferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND"));
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notif);
        r.play();
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);
    }
}