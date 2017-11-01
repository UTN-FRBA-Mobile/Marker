package com.marker.app;

import com.marker.facebook.User;
import com.marker.lugar.destino.Destino;

import java.util.ArrayList;
import java.util.List;

public class Marcador {
    private int metrosDeteccion;
    private User user;
    private Destino destino;
    private List<String> usuarios = new ArrayList<>();
    private String id;

    //Lo usa Firebase.. NO BORRAR
    Marcador() {}

    public Marcador(User user, Destino destino, int metrosDeteccion) {
        this.destino = destino;
        this.user = user;
        this.metrosDeteccion = metrosDeteccion;
    }

    public Destino getDestino() {
        return destino;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Marcador && id != null) {
            Marcador otro = (Marcador) obj;
            return id.equals(otro.id);
        }
        return super.equals(obj);
    }
}
