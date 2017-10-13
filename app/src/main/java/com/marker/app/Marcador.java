package com.marker.app;

import com.marker.facebook.User;
import com.marker.lugar.Lugar;

import java.util.ArrayList;
import java.util.List;

public class Marcador {
    private int metrosDeteccion;
    private User user;
    private Lugar lugar;
    private List<String> usuarios = new ArrayList<>();

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

    public List<String> getUsuarios() {
        return usuarios;
    }

    public int getMetrosDeteccion() {
        return metrosDeteccion;
    }
}
