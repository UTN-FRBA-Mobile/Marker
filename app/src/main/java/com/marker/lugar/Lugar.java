package com.marker.lugar;

import android.os.Parcelable;

import com.marker.locator.LatLong;

/**
 * Created by sdamilano on 16/10/17.
 */

public abstract class Lugar implements Parcelable{

    public String uid;
    public String nombre;
    public LatLong posicion;

}
