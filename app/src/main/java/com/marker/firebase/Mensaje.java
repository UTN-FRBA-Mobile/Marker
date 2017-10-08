package com.marker.firebase;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class Mensaje {
    private static final String CLAVE_MENSAJE = "body";
    @SerializedName("to")
    private String tokenReceptor;
    @SerializedName("notification")
    private HashMap<String, String> datos = new HashMap<>();

    public Mensaje(String tokenReceptor, String mensaje) {
        this.tokenReceptor = tokenReceptor;
        setMensaje(mensaje);
    }

    public void setMensaje(String mensaje) {
        datos.put(CLAVE_MENSAJE, mensaje);
        datos.put("title","Mensaje desde mi propio celu :D");
    }

    @Nullable
    public String getMensaje() {
        return datos.get(CLAVE_MENSAJE);
    }
}
