package com.marker.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marker.facebook.User;

public class EmisorMensajes {

    public void enviar(User usuario, final Mensaje fcm) {
        FirebaseDatabase.getInstance()
            .getReference("usuarios/" + usuario.getId() + "/token")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    fcm.setToken((String) dataSnapshot.getValue());
                    FirebaseDatabase.getInstance()
                        .getReference("/fcm")
                        .push()
                        .setValue(fcm);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }
}