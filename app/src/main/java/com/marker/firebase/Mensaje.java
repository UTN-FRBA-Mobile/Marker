package com.marker.firebase;

import android.support.annotation.Nullable;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;

public class Mensaje {
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";

    @PropertyName("to")
    private String tokenReceptor;
    private HashMap<String, Object> payload = new HashMap<>();
    private Boolean esData;

    private Mensaje(boolean esData) {
        this.esData = esData;
    }

    public void setBody(String body) {
        payload.put(KEY_BODY, body);
    }

    public void setTitle(String title) {
        payload.put(KEY_TITLE, title);
    }

    @Nullable
    String getBody() {
        return (String) payload.get(KEY_BODY);
    }

    @Nullable
    String getTitle() {
        return (String) payload.get(KEY_TITLE);
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

    public void setEsData(Boolean esData) {
        this.esData = esData;
    }

    public Boolean getEsData() {
        return esData;
    }

    public String getTokenReceptor() {
        return tokenReceptor;
    }

    public void setToken(String token) {
        this.tokenReceptor = token;
    }

    public static Mensaje newNotification() {
        return new Mensaje(false);
    }

    public static Mensaje newDataMessage() {
        return new Mensaje(true);
    }
}
