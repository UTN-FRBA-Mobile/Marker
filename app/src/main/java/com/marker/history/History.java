package com.marker.history;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class History implements Parcelable {
    public String location;
    public Date date;
    public LatLng position;

    History(String location, Date date, LatLng position){
        this.location = location;
        this.date = date;
        this.position = position;
    }

    public static final List<History> initializeData(){
        List<History> histories = new ArrayList<>();
        histories.add(new History("Plaza Italia - CABA",
                                    new GregorianCalendar(2017, Calendar.JULY, 11).getTime(),
                                    new LatLng(-34.581075, -58.421060)));
        histories.add(new History("Medrano 951 - CABA",
                                    new GregorianCalendar(2017, Calendar.AUGUST, 16).getTime(),
                                    new LatLng(-34.598608, -58.419917)));
        histories.add(new History("Mozart 2300 - CABA",
                                    new GregorianCalendar(2017, Calendar.JULY, 11).getTime(),
                                    new LatLng(-34.656431, -58.468422)));
        return histories;
    }

    // Parcelling part
    public History(Parcel in){
        String[] data = new String[4];

        DateFormat format = new SimpleDateFormat("yyyy mmmm dd", Locale.ENGLISH);

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.location = data[0];
        try {
            this.date = format.parse(data[1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        DateFormat format = new SimpleDateFormat("yyyy mmmm dd", Locale.ENGLISH);
        dest.writeStringArray(new String[]
                { this.location,
                format.format(this.date),
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
