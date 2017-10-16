package com.marker.destino.lugar;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.destino.Destino;
import com.marker.locator.LatLong;

public class Lugar extends Destino{

    public String urlImagen;

    public Lugar(){}

    public Lugar(String nombre, String urlImagen, LatLong posicion) {
        this.nombre = nombre;
        this.urlImagen = urlImagen;
        this.posicion = posicion;
    }

    // Parcelling part
    public Lugar(Parcel in){
        String[] data = new String[5];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.uid = data[0];
        this.nombre = data[1];
        this.urlImagen = data[2];
        double lat = Double.parseDouble(data[3]);
        double lon = Double.parseDouble(data[4]);
        this.posicion = new LatLong(lat, lon);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                {   this.uid,
                    this.nombre,
                    this.urlImagen,
                    String.valueOf(this.posicion.latitude),
                    String.valueOf(this.posicion.longitude)
                });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Lugar createFromParcel(Parcel in) {
            return new Lugar(in);
        }

        public Lugar[] newArray(int size) {
            return new Lugar[size];
        }
    };
}
