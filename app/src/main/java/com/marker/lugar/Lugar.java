package com.marker.lugar;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Lugar implements Parcelable{
    public String nombre;
    public String urlImagen;
    public LatLng position;

    public Lugar(String nombre, String urlImagen, LatLng position) {
        this.nombre = nombre;
        this.urlImagen = urlImagen;
        this.position = position;
    }

    // Parcelling part
    public Lugar(Parcel in){
        String[] data = new String[4];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.nombre = data[0];
        this.urlImagen = data[1];
        double lat = Double.parseDouble(data[2]);
        double lon = Double.parseDouble(data[3]);
        this.position = new LatLng(lat, lon);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                {   this.nombre,
                    this.urlImagen,
                    String.valueOf(this.position.latitude),
                    String.valueOf(this.position.longitude)
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
