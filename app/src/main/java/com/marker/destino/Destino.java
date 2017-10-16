package com.marker.destino;

import android.os.Parcelable;

import com.marker.locator.LatLong;

/**
 * Created by sdamilano on 16/10/17.
 */

public abstract class Destino implements Parcelable{

    public String uid;
    public String nombre;
    public LatLong posicion;


//    LatLong posicion = new LatLong(destination.getLatLng().latitude, destination.getLatLng().longitude);
//    String nombre = destination.getName().toString();
}
