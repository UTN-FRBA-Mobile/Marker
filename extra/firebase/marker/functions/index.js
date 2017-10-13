// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.onAddFCM = functions.database.ref("/fcm/{pushId}")
    .onCreate(event => {
        const fcm = event.data.val();
        const token = fcm.tokenReceptor;
        const campo = fcm.esData ? "data" : "notification";
        const payload = {};
        payload[campo] = fcm.payload;
        enviar = admin.messaging().sendToDevice([token], payload);
        eliminarMsj = event.data.adminRef.remove();
        return Promise.all([enviar, eliminarMsj]);
    });

exports.onAddMarker = functions.database.ref("/usuarios/{uid}/markers/{pushId}")
    .onWrite(event => {
        const marker = event.data.val();
        const usuariosIds = marker.usuarios;
        const pushId = event.params.pushId;
        const idContainer = {};
        const payload = {
              notification: {
                title: `Nuevo Marker de ${marker.user.name}!`,
                body: 'Toca para ver',
              }
            };
        for(var index in usuariosIds) {
            var usuarioId = usuariosIds[index];
            admin.database()
                .ref("/usuarios/"+usuarioId+"/token")
                .once("value", function(snap) {
//                    admin.database()
//                        .ref('/pruebas/'+pushId)
//                        .child(snap.ref.parent.key)
//                        .set(snap.val());
                    admin.messaging().sendToDevice([snap.val()], payload);
                });
        }
    });