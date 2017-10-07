package com.marker.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.locator.LatLng;


public class History implements Parcelable {
    public String uid;
    public String location;
    public LatLng position;

    public History(){
        // Default constructor required for calls to DataSnapshot.getValue(History.class)
    }

    public History(String uid, String location, LatLng position){
        this.uid = uid;
        this.location = location;
        this.position = position;
    }

    // Parcelling part
    public History(Parcel in){
        String[] data = new String[4];


        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.uid = data[0];
        this.location = data[1];
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
                { this.uid,
                this.location,
                String.valueOf(this.position.latitude),
                String.valueOf(this.position.longitude)
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
}
