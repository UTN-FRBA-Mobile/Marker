package com.marker.lugar.history;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marker.facebook.User;
import com.marker.locator.LatLong;
import com.marker.lugar.Lugar;

import java.util.ArrayList;

public class HistoryManager {
    private static final String TAG = "History";
    private static DatabaseReference mDatabase;
    public  ArrayList<History> histories = new ArrayList<>();
    private User user;

    public HistoryManager(User user){
        this.user = user;
    }

    public void requestHistories() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(user.getId())
                .child("histories")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    History history = snapshot.getValue(History.class);
                    histories.add(history);
                }
                onGetHistories(histories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void onGetHistories(ArrayList<History> histories) {
    }

    public void addPlace(final Lugar destination){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(user.getId())
                .child("histories")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Borro el history si ya existe
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    History history = snapshot.getValue(History.class);

                    if(history.nombre.equals(destination.nombre)) {
                        deleteHistory(history.uid);
                    }
                }
                writeHistory(destination.nombre, destination.posicion);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void writeHistory(String nombre, LatLong posicion) {
        String uid = mDatabase.child("usuarios").child(user.getId()).child("histories").push().getKey();
        History history = new History(nombre, posicion);
        history.setCurrentTime();
        history.uid = uid;
        mDatabase.child("usuarios").child(user.getId()).child("histories").child(uid).setValue(history);
    }

    private void deleteHistory(String uid){
        mDatabase.child("usuarios").child(user.getId()).child("histories").child(uid).removeValue();
    }
}
