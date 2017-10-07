package com.marker.history;

import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marker.locator.LatLng;

import java.util.ArrayList;
import java.util.Iterator;

public class HistoryManager {
    private static final String TAG = "History";
    private static DatabaseReference mDatabase;
    public  ArrayList<History> histories = new ArrayList<>();
    private String userId;

    public HistoryManager(String userId){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                History history = dataSnapshot.getValue(History.class);
                histories.add(history);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                History history = dataSnapshot.getValue(History.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String historyKey = dataSnapshot.getKey();
                Iterator<History> iter = histories.iterator();
                while (iter.hasNext()) {
                    History h = iter.next();
                    if (h.uid == historyKey) iter.remove();
                }
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
        mDatabase.child("histories").child(userId).addChildEventListener(childEventListener);

    }

    public void writePlace(Place place){
        LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        this.writeHistory(place.getName().toString(), latLng);
    }

    public void writeHistory(String location, LatLng position) {
        String uid = mDatabase.child("histories").child(userId).push().getKey();
        History history = new History(uid, location, position);
        mDatabase.child("histories").child(userId).child(uid).setValue(history);
    }
}
