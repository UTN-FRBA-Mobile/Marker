package com.marker.destino.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.destino.Destino;
import com.marker.locator.LatLong;

import java.util.Date;
import java.util.GregorianCalendar;


public class History extends Destino implements Parcelable{

    public Date datetime;

    public History(){
        // Default constructor required for calls to DataSnapshot.getValue(History.class)
    }

    public History(String nombre, LatLong posicion){
        this.nombre = nombre;
        this.posicion = posicion;
    }

    // Parcelling part
    public History(Parcel in){
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.nombre = data[0];
        double lat = Double.parseDouble(data[1]);
        double lon = Double.parseDouble(data[2]);
        this.posicion = new LatLong(lat, lon);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                { this.nombre,
                    String.valueOf(this.posicion.latitude),
                    String.valueOf(this.posicion.longitude)
                });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public History createFromParcel(Parcel in) {
            return new History(in);
        }

        public History[] newArray(int size) {
            return new History[size];
        }
    };

    public void setCurrentTime() {
        this.datetime = GregorianCalendar.getInstance().getTime();
    }
}
