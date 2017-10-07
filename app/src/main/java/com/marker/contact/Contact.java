package com.marker.contact;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Contact implements Parcelable {
    public String name;
    String phone;
    String email;
    Boolean checked;

    public Contact(String name, String phone, String email){
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.checked = false;
    }

    public static final ArrayList<Contact> initializeData(){
        ArrayList<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Eze", "1155555555", "eze@android.com"));
        contacts.add(new Contact("Fer", "1166666666", "fer@android.com"));
        contacts.add(new Contact("Fran", "1177777777", "fran@android.com"));
        contacts.add(new Contact("Sandro", "1188888888", "sandro@android.com"));
        return contacts;
    }

    // Parcelling part
    public Contact(Parcel in){
        String[] data = new String[4];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.name = data[0];
        this.phone = data[1];
        this.email = data[2];
        this.checked = Boolean.valueOf(data[3]);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                { this.name,
                  this.phone,
                  this.email,
                  String.valueOf(this.checked)
                });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
};