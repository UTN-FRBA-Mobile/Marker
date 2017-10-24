package com.marker.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marker.app.GestorSesion;
import com.marker.facebook.User;

public class EmisorMensajes {

    public void enviar(User user, Mensaje fcm) {
        enviar(user.getId(), fcm);
    }

    public void enviar(String uid, final Mensaje fcm) {
        fcm.setIdReceptor(uid);
        fcm.setIdEmisor(GestorSesion.getInstancia().getUsuarioLoggeado().getId());
        FirebaseDatabase.getInstance()
                .getReference("/fcm")
                .push()
                .setValue(fcm);
    }
}