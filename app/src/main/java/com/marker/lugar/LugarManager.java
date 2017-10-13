package com.marker.lugar;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marker.locator.LatLong;

import java.util.ArrayList;

public class LugarManager implements Parcelable{
    private static final String TAG = "Lugar";
    private static DatabaseReference mDatabase;
    public  ArrayList<Lugar> lugares = new ArrayList<>();
    private String userId;

    public LugarManager(String userId){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Lugar lugar = dataSnapshot.getValue(Lugar.class);
                lugares.add(lugar);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                Lugar lugar = dataSnapshot.getValue(Lugar.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                Lugar removedLugar = dataSnapshot.getValue(Lugar.class);

                // FML
                int position = 0;
                for (Lugar lugar : lugares) {
                    if(lugar.uid.equals(removedLugar.uid))
                        break;
                    position += 1;
                }

                lugares.remove(position);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };

        this.userId = userId;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("usuarios").child(userId).child("lugares").addChildEventListener(childEventListener);

    }

    protected LugarManager(Parcel in) {
        lugares = in.createTypedArrayList(Lugar.CREATOR);
        userId = in.readString();
    }

    public static final Creator<LugarManager> CREATOR = new Creator<LugarManager>() {
        @Override
        public LugarManager createFromParcel(Parcel in) {
            return new LugarManager(in);
        }

        @Override
        public LugarManager[] newArray(int size) {
            return new LugarManager[size];
        }
    };

    public void writePlace(Place place){
        LatLong latLong = new LatLong(place.getLatLng().latitude, place.getLatLng().longitude);
        this.writeLugar(place.getName().toString(), latLong);
    }

    public void writeLugar(String location, LatLong position) {
        String uid = mDatabase.child("usuarios").child(userId).child("lugares").push().getKey();
        Lugar lugar = new Lugar(location, "", position);
        lugar.uid = uid;
        mDatabase.child("usuarios").child(userId).child("lugares").child(uid).setValue(lugar);
    }

    public void deleteLugar(String uid){
        mDatabase.child("usuarios").child(userId).child("lugares").child(uid).removeValue();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeList(this.lugares);
    }
}