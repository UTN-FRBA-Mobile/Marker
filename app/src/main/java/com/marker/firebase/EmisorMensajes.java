package com.marker.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marker.facebook.User;

public class EmisorMensajes {

    public void enviar(User user, Mensaje fcm) {
        enviar(user.getId(), fcm);
    }

    public void enviar(String uid, final Mensaje fcm) {
        FirebaseDatabase.getInstance()
            .getReference("usuarios/" + uid + "/token")
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