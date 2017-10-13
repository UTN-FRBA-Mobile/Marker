package com.marker.firebase;

import android.support.annotation.Nullable;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;

public class Mensaje {
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";

    @PropertyName("to")
    private String tokenReceptor;
    private HashMap<String, String> payload = new HashMap<>();
    private Boolean esData;

    public Mensaje(String tokenReceptor) {
        this.tokenReceptor = tokenReceptor;
    }

    public void setBody(String body) {
        payload.put(KEY_BODY, body);
    }

    public void setTitle(String title) {
        payload.put(KEY_TITLE, title);
    }

    @Nullable
    String getBody() {
        return payload.get(KEY_BODY);
    }

    @Nullable
    String getTitle() {
        return payload.get(KEY_TITLE);
    }

    public HashMap<String, String> getPayload() {
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
}
