package com.marker.firebase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.marker.R;
import com.marker.facebook.User;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class EmisorMensajes implements ValueEventListener {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private final OkHttpClient mClient = new OkHttpClient();
    private final Context context;
    private String cuerpoMensajeAEnviar;

    public EmisorMensajes(Context context) {
        this.context = context;
    }

    public void enviar(User usuario, final String cuerpoMensaje) {
        this.cuerpoMensajeAEnviar = cuerpoMensaje;
        FirebaseDatabase.getInstance()
                .getReference("usuarios/" + usuario.getId() + "/token")
                .addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        final String receptorToken = (String) dataSnapshot.getValue();
        if (receptorToken == null) {
            //No hay token registrado para este usuario.
            return;
        }
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    Mensaje fcmMessage = new Mensaje(receptorToken);
                    cuerpoMensajeAEnviar = null;
                    String stringify = new Gson().toJson(fcmMessage);
                    String result = postToFCM(stringify);
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                //Nada
            }
        }.execute();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    private String postToFCM(String bodyString) throws IOException {
        String serverKey = context.getResources().getString(R.string.FIREBASE_SERVER_KEY);
        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + serverKey)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }
}