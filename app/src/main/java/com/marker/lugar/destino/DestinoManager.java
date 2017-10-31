package com.marker.lugar.destino;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marker.facebook.User;
import com.marker.locator.LatLong;
import com.marker.lugar.Lugar;
import com.marker.lugar.history.History;

import java.util.ArrayList;

public class DestinoManager {
    private static final String TAG = "Destino";
    private static DatabaseReference mDatabase;
    public  ArrayList<Destino> destinos = new ArrayList<>();
    private User user;

    public DestinoManager(User user){
        this.user = user;
    }

    public void requestDestinos() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(user.getId())
                .child("destinos")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Destino destino = snapshot.getValue(Destino.class);
                    destinos.add(destino);
                }
                onGetDestinos(destinos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void onGetDestinos(ArrayList<Destino> destinos) {
    }

    public void checkDestino(final Lugar destination){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(user.getId())
                .child("destinos")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Reviso si el destino si ya existe
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Destino destino = snapshot.getValue(Destino.class);

                    if(destino.nombre.equals(destination.nombre)) {
                        onCheckDestino(false);
                        return;
                    }
                }
                onCheckDestino(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void onCheckDestino(boolean result) {
    }



    public void writeDestino(String location, LatLong position) {
        String uid = mDatabase.child("usuarios").child(user.getId()).child("destinos").push().getKey();
        Destino destino = new Destino(location, "", position);
        destino.uid = uid;
        mDatabase.child("usuarios").child(user.getId()).child("destinos").child(uid).setValue(destino);
    }

    public void deleteDestino(String uid){
        mDatabase.child("usuarios").child(user.getId()).child("destinos").child(uid).removeValue();
    }

}
