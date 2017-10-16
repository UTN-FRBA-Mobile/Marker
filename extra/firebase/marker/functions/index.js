// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.onAddFCM = functions.database.ref("/fcm/{pushId}")
    .onCreate(event => {
        const fcm = event.data.val();
        const token = fcm.tokenReceptor;
        //console.log(token);
        const campo = fcm.esData ? "data" : "notification";
        const payload = {};
        payload[campo] = fcm.payload;
        enviar = admin.messaging().sendToDevice([token], payload);
        eliminarMsj = event.data.adminRef.remove();
        return Promise.all([enviar, eliminarMsj]);
    });

exports.onAddMarker = functions.database.ref("/usuarios/{uid}/markers/{pushId}")
    .onCreate(event => {
        const marker = event.data.val();
        const markerUid = marker.user.id;
        const uid = event.params.uid;
        if (uid != markerUid) {
            //Es una creacion de un marker ajeno. No se debe volver a compartir.
            console.log("Evitando propagacion luego de creacion de " + event.data.ref);
            return 0;
        }
        const usuariosIds = marker.usuarios;
        const pushId = event.params.pushId;
        const payload = {
              notification: {
                title: `Nuevo Marker de ${marker.user.name}!`,
                body: 'Toca para ver',
                icon: markerUid
              }, data: {
                "marker": JSON.stringify(marker),
                "tipoData": "MARKER"
              }
            };
        const promises = [];
        for(var index in usuariosIds) {
            var userId = usuariosIds[index];
            var notificacion = admin.database()
                .ref(`/usuarios/${userId}/token`)
                .once("value", function(snap) {
                    admin.messaging().sendToDevice([snap.val()], payload);
                });
            var escribir = admin.database()
                .ref(`/usuarios/${userId}/markers/${pushId}`)
                .set(marker)
            promises.push(notificacion);
            promises.push(escribir);
        }
        return Promise.all(promises);
    });