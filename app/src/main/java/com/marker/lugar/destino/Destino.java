package com.marker.lugar.destino;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.lugar.Lugar;
import com.marker.locator.LatLong;

public class Destino extends Lugar {

    public String urlImagen;

    public Destino(){}

    public Destino(String nombre, String urlImagen, LatLong posicion) {
        this.nombre = nombre;
        this.urlImagen = urlImagen;
        this.posicion = posicion;
    }

    // Parcelling part
    public Destino(Parcel in){
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
        public Destino createFromParcel(Parcel in) {
            return new Destino(in);
        }

        public Destino[] newArray(int size) {
            return new Destino[size];
        }
    };
}
