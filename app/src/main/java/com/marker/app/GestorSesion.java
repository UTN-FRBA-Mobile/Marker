package com.marker.app;

import android.content.Context;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.marker.facebook.User;
import com.marker.firebase.EmisorMensajes;
import com.marker.lugar.Lugar;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**Singleton para gestionar lo que ocurre en la app
 */
public class GestorSesion {
    private static GestorSesion singleton;
    //Markers activos
    private final ArrayList<Marcador> marcadors = new ArrayList<>();
    private final EventoObservable onInicializado = new EventoObservable();
    //Facebook token
    private AccessToken token;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private User usuarioYo;
    private User me;
    private User[] friends;
    private EmisorMensajes emisor;

    public static GestorSesion getInstancia(){
        if (singleton == null) {
            singleton = new GestorSesion();
        }
        return singleton;
    }

    private GestorSesion() {
    }

    /**Inicializa el usuario y amigos
     * @throws Exception Si se llama a este metodo sin estar loggeado
     */
    public void inicializar(Context context) throws Exception {
        token = AccessToken.getCurrentAccessToken();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null || token == null) {
            throw new Exception("Debes loggearte antes de inicializar la sesion");
        }
        emisor = new EmisorMensajes(context);
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        GraphRequest request;
        request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                me = new Gson().fromJson(jsonObject.toString(), User.class);
                notificarInicializacion();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
        request = GraphRequest.newMyFriendsRequest(token, new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray objects, GraphResponse response) {
                friends = new Gson().fromJson(objects.toString(), User[].class);
                notificarInicializacion();
            }
        });
        request.executeAsync();
    }

    private void notificarInicializacion() {
        if (inicializado()) {
            actualizarTokenEnServidor();
            onInicializado.notificar();
        }
    }

    public boolean inicializado() {
        return me != null && friends != null;
    }

    public boolean loggeado() {
        return mAuth != null && token != null;
    }

    public ArrayList<Marcador> getMarcadores() {
        return marcadors;
    }

    /**
     * Crea un marker con mi propio usuario
     * @param lugar lugar del marker
     * @param radioDeteccion radio de deteccion del marker
     * @return marker creado
     */
    public Marcador crearMarcador(Lugar lugar, int radioDeteccion) {
        User me = SerializationUtils.clone(this.me);
        me.setName(String.format("%s (Yo)", me.getName()));
        Marcador marcador = new Marcador(me, lugar, radioDeteccion);

        //todo controlar que no haya otro marcador que me trakee a mi mismo.
        marcadors.add(marcador);

        return marcador;
    }

    public static void actualizarTokenEnServidor() {
        User me = getInstancia().me;
        if (me == null) {
            return;
        }
        String uid = me.getId();
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance()
                .getReference("usuarios/"+uid+"/token")
                .setValue(token);
    }

    public EventoObservable getOnInicializado() {
        return onInicializado;
    }

    public FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public User getUsuarioLoggeado() {
        return me;
    }

    public User[] getFriends() {
        return friends;
    }

    public EmisorMensajes getEmisorMensajes() {
        return emisor;
    }
}