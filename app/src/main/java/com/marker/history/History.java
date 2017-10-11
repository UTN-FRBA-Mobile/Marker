package com.marker.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.marker.locator.LatLong;


public class History implements Parcelable {
    public String location;
    public LatLong position;

    public History(){
        // Default constructor required for calls to DataSnapshot.getValue(History.class)
    }

    public History(String location, LatLong position){
        this.location = location;
        this.position = position;
    }

    // Parcelling part
    public History(Parcel in){
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.location = data[0];
        double lat = Double.parseDouble(data[1]);
        double lon = Double.parseDouble(data[2]);
        this.position = new LatLong(lat, lon);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                { this.location,
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
