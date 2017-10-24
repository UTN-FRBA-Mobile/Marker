package com.marker.firebase;

import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.gson.Gson;
import com.marker.app.Marcador;

import java.util.HashMap;
import java.util.Map;

public class Mensaje {
    private static final String KEY_TITLE = "title";
    private static final String KEY_BODY = "body";
    private static final String KEY_TIPODATA = "tipoData";
    private static final String KEY_MARKER = "marker";
    private Map<String, String> payload = new HashMap<>();
    Boolean esData;
    String idReceptor;
    String idEmisor;

    public void setIdReceptor(String idReceptor) {
        this.idReceptor = idReceptor;
    }

    public void setIdEmisor(String idEmisor) {
        this.idEmisor = idEmisor;
    }

    public enum TipoData {MARKER, PEDIDO_POSICION, POSICION;}

    private Mensaje(boolean esData) {
        this.esData = esData;
    }

    public static Mensaje newNotification() {
        return new Mensaje(false);
    }

    public static Mensaje newDataMessage() {
        return new Mensaje(true);
    }

    public static Mensaje newDataMessage(Map<String, String> payload) {
        Mensaje mensaje = newDataMessage();
        mensaje.payload = payload;
        return mensaje;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    ///////////////////////
    //Si es Notificacion //
    ///////////////////////
    public void setBody(String body) {
        payload.put(KEY_BODY, body);
    }

    public void setTitle(String title) {
        payload.put(KEY_TITLE, title);
    }

    @Nullable
    @Exclude
    String getBody() {
        return payload.get(KEY_BODY);
    }

    @Nullable
    @Exclude
    String getTitle() {
        return payload.get(KEY_TITLE);
    }

    ///////////////
    //Si es data //
    ///////////////
    public void setTipoData(TipoData tipo) {
        payload.put(KEY_TIPODATA, tipo.name());
    }

    public void setMarker(Marcador marker) {
        payload.put(KEY_MARKER, new Gson().toJson(marker));
    }

    @Nullable
    @Exclude
    public TipoData getTipoData() {
        String tipo = payload.get(KEY_TIPODATA);
        if (tipo != null) {
            return TipoData.valueOf(tipo);
        }
        return null;
    }

    @Nullable
    @Exclude
    public Marcador getMarker() {
        String marker = payload.get(KEY_MARKER);
        if (marker != null) {
            return new Gson().fromJson(marker, Marcador.class);
        }
        return null;
    }
}