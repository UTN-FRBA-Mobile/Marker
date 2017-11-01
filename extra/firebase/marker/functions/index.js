// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.onAddFCM = functions.database.ref("/fcm/{pushId}")
    .onCreate(event => {
        const fcm = event.data.val();
        const idEmisor = fcm.idEmisor;
        const idReceptores = fcm.idReceptores;
        if (!idEmisor || !idReceptores) {
            console.log("Se interrumpe FCM por campos incompletos. Falta idEmisor o idReceptor");
            return -1;
        }
        console.log("idEmisor: " + idEmisor);

        var fcmPayload = fcm.payload;
        fcmPayload.idEmisor = idEmisor;

        var promisesTokensId = idReceptores.map(function(id) {
            console.log("idReceptor: " + id);
            return admin.database()
               .ref(`/usuarios/${id}/token`)
               .once("value");
        });

        return Promise.all(promisesTokensId)
            .then(function(snapshots) {
                var promises = [];
                var campo = fcm.esData ? "data" : "notification";
                var payload = {};
                payload[campo] = fcmPayload;

                var tokens = snapshots
                    .map(s => {
                        console.log("Token receptor: " + s.val());
                        return s.val();
                    }).filter(t => t);

                var enviar = admin.messaging().sendToDevice(tokens, payload);
                var eliminarMsj = event.data.adminRef.remove();
                promises.push(enviar, eliminarMsj);

                //Analizar tipoMensaje en caso de poder entregar mejores datos
                const tipoData = fcmPayload.tipoData;
                if (tipoData == "PEDIDO_POSICION" &&
                    idReceptores.length == 1) {
                    promises.push(onPedidoPosicion(idEmisor, idReceptores[0]));
                }
                if (tipoData == "POSICION") {
                    promises.push(onPosicion(idEmisor, fcmPayload.posicion));
                }
                return Promise.all(promises);
        });
    });

const onPosicion = function(idEmisor, pos) {
    //Guardar la posicion en el usuario para cachearla
    console.log("Se persiste posicion para cachearla.");
    return admin.database()
        .ref(`/usuarios/${idEmisor}/ubicacion`)
        .set(pos);
};

const onPedidoPosicion = function(idEmisor, idReceptor) {
    //Contestar al que pregunta con la posicion cacheada
    var d = {};
    return admin.database()
        .ref(`/usuarios/${idReceptor}/ubicacion`)
        .once("value")
        .then(function(snapPosicion) {
            var pos = snapPosicion.val();
            if (pos == null) {
                console.log("No hay posicion cacheada.");
            } else {
                console.log("Se logra obtener posicion cacheada.");
                d.pos = pos;
            }
            return admin.database()
                .ref(`/usuarios/${idEmisor}/token`)
                .once("value");
        }).then(function(snapTokenEmisor) {
            if (!d.pos) {
                return 0;
            }
            console.log("Token emisor: " + snapTokenEmisor.val());
            var p = { "data": {}};
            p.data.tipoData = "POSICION";
            p.data.posicion = d.pos;
            p.data.idEmisor = idReceptor;
            return admin.messaging()
                .sendToDevice([snapTokenEmisor.val()], p);
        });
};

exports.onFcm1 = functions.database.ref("/fcm1/{pushId}")
    .onCreate(event => {
        const fcm = event.data.val();
        const uid = fcm.uid;
        admin.database()
            .ref(`/usuarios/${uid}/token`)
            .once("value")
            .then(function(snapshot) {
                var token = snapshot.val();
                return event.data.adminRef.child("token").set(token);
            }).then(function() {
                console.log("Token persistido");
            });
        return 0;
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