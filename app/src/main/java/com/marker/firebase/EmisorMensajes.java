package com.marker.firebase;

import com.google.firebase.database.FirebaseDatabase;
import com.marker.app.GestorSesion;
import com.marker.facebook.User;

public class EmisorMensajes {

    public void enviar(User fromUser, User user, Mensaje fcm) {
        enviar(fromUser.getId(), user.getId(), fcm);
    }

    public void enviar(String fromUid, String toUid, final Mensaje fcm) {
        fcm.setIdReceptor(toUid);
        fcm.setIdEmisor(fromUid);
        FirebaseDatabase.getInstance()
                .getReference("/fcm")
                .push()
                .setValue(fcm);
    }
}