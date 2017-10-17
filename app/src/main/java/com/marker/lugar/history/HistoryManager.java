package com.marker.lugar.history;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marker.app.EventoObservable;
import com.marker.lugar.Lugar;
import com.marker.locator.LatLong;

import java.util.ArrayList;

public class HistoryManager {
    private static final String TAG = "History";
    private static DatabaseReference mDatabase;
    public  ArrayList<History> histories = new ArrayList<>();
    private String userId;
    private EventoObservable onInicializado = new EventoObservable();

    public void inicializar(String userId){
        final ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                History history = dataSnapshot.getValue(History.class);
                if (!histories.contains(history)) {
                    histories.add(history);
                }
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
                History removedHistory = dataSnapshot.getValue(History.class);

                // FML
                int position = 0;
                for (History history : histories) {
                    if(history.uid.equals(removedHistory.uid))
                        break;
                    position += 1;
                }

                histories.remove(position);
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
        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(userId)
                .child("histories")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    History history = snapshot.getValue(History.class);
                    histories.add(history);
                }
                onInicializado.notificar();
                orderedQuery.addChildEventListener(childEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addPlace(Lugar destination){
        String nombre = destination.nombre;

        // Borro el history si ya existe
        for(History history : histories){
            if(history.nombre.equals(nombre))
                deleteHistory(history.uid);
        }

        this.writeHistory(nombre, destination.posicion);
    }

    public void writeHistory(String nombre, LatLong posicion) {
        String uid = mDatabase.child("usuarios").child(userId).child("histories").push().getKey();
        History history = new History(nombre, posicion);
        history.setCurrentTime();
        history.uid = uid;
        mDatabase.child("usuarios").child(userId).child("histories").child(uid).setValue(history);
    }

    private void deleteHistory(String uid){
        mDatabase.child("usuarios").child(userId).child("histories").child(uid).removeValue();
    }

    public EventoObservable getOnInicializado() {
        return onInicializado;
    }
}
