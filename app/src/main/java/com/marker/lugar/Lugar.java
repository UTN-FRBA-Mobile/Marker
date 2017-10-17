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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Lugar) {
            Lugar otro = (Lugar) obj;
            return uid != null && uid.equals(otro.uid);
        }
        return super.equals(obj);
    }
}
