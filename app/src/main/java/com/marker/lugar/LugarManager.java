package com.marker.lugar;

import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marker.locator.LatLong;

import java.util.ArrayList;

public class LugarManager {
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
                Lugar removedHistory = (Lugar) dataSnapshot.getValue();
                lugares.remove(removedHistory);
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
}
