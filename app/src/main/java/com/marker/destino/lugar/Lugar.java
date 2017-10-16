package com.marker.destino.lugar;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.locator.LatLong;

public class Lugar implements Parcelable{
    public String uid;
    public String nombre;
    public String urlImagen;
    public LatLong position;

    public Lugar(){}

    public Lugar(String nombre, String urlImagen, LatLong position) {
        this.nombre = nombre;
        this.urlImagen = urlImagen;
        this.position = position;
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
        this.position = new LatLong(lat, lon);
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
