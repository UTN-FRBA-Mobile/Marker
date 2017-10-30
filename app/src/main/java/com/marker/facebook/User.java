package com.marker.facebook;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Serializable, Parcelable {
    private String id;
    private String name;
    private String email;

    public User() {}

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (id == null) return super.equals(obj);
        if (obj != null && obj instanceof User) {
            return id.equals(((User)obj).id);
        } else {
            return super.equals(obj);
        }
    }


    // Parcelling part
    public User(Parcel in){
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.id = data[0];
        this.name = data[1];
        this.email = data[2];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]
                {   this.id,
                        this.name,
                        this.email
                });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
