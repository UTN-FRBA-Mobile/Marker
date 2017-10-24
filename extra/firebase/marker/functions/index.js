// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.onAddFCM = functions.database.ref("/fcm/{pushId}")
    .onCreate(event => {
        const fcm = event.data.val();
        const idEmisor = fcm.idEmisor;
        const idReceptor = fcm.idReceptor;
        if (!idEmisor || !idReceptor) {
            console.log("Se interrumpe FCM por campos incompletos. Falta idEmisor o idReceptor");
            return -1;
        }
        console.log("idReceptor: " + idReceptor);
        console.log("idEmisor: " + idEmisor);
        return admin.database()
            .ref(`/usuarios/${idReceptor}/token`)
            .once("value", function(snap) {
                const token = snap.val();
                if (token == null) {
                    console.log("No se pudo obtener token de acceso");
                    return -1;
                }
                console.log("Token receptor: " + token);
                const campo = fcm.esData ? "data" : "notification";
                const payload = {};
                const fcmPayload = fcm.payload;
                payload[campo] = fcmPayload;
                //Se injecta el idEmisor
                fcmPayload.idEmisor = idEmisor;
                enviar = admin.messaging().sendToDevice([token], payload);
                eliminarMsj = event.data.adminRef.remove();
                var promises = [enviar, eliminarMsj];
                const tipoData = fcmPayload.tipoData;
                if (fcm.esData && tipoData) {
                    var otraOperacion;
                    switch(tipoData) {
                        case "PEDIDO_POSICION":
                            //Contestar al que pregunta con la posicion cacheada
                            otraOperacion = admin.database()
                                .ref(`/usuarios/${idReceptor}/ubicacion`)
                                .once("value", function(snap1) {
                                var pos = snap1.val();
                                if (pos == null) {
                                    console.log("No hay posicion cacheada.");
                                    return;
                                }
                                console.log("Se logra obtener posicion cacheada.");
                                var payloadUbicacion = { "data": {}};
                                payloadUbicacion.data.tipoData = "POSICION";
                                payloadUbicacion.data.posicion = pos;
                                payloadUbicacion.data.idEmisor = idReceptor;
                                return admin.database()
                                    .ref(`/usuarios/${idEmisor}/token`)
                                    .once("value", function(snap2) {
                                        console.log("Token emisor: " + snap2.val());
                                        return admin.messaging()
                                            .sendToDevice([snap2.val()], payloadUbicacion);
                                    });
                                return admin.messaging().sendToDevice([token], payloadUbicacion);
                            });
                        break;
                        case "POSICION":
                            //Guardar la posicion en el usuario para cachearla
                            console.log("Se persiste posicion para cachearla.");
                            otraOperacion = admin.database()
                                .ref(`/usuarios/${idEmisor}/ubicacion`)
                                .set(fcmPayload.posicion);
                        break;
                    }
                    promises.push(otraOperacion);
                }
                return Promise.all(promises);
            });
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