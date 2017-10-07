package com.marker.app;

import com.marker.facebook.User;
import com.marker.lugar.Lugar;

public class Marcador {
    private int metrosDeteccion;
    private User user;
    private Lugar lugar;

    Marcador(User user, Lugar lugar, int metrosDeteccion) {
        this.lugar = lugar;
        this.user = user;
        this.metrosDeteccion = metrosDeteccion;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public User getUser() {
        return user;
    }
}
