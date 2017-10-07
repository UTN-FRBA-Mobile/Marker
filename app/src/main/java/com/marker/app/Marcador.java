package com.marker.app;

import com.marker.friends.Contact;
import com.marker.lugar.Lugar;

public class Marcador {
    private int metrosDeteccion;
    private Contact contacto;
    private Lugar lugar;

    Marcador(Contact aTrakear, Lugar lugar, int metrosDeteccion) {
        this.lugar = lugar;
        this.contacto = aTrakear;
        this.metrosDeteccion = metrosDeteccion;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public Contact getContacto() {
        return contacto;
    }
}
