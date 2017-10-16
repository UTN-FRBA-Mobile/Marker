package com.marker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.marker.facebook.User;
import com.marker.firebase.EmisorMensajes;
import com.marker.lugar.destino.Destino;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**Singleton para gestionar lo que ocurre en la app
 */
public class GestorSesion {
    private static final String TAG = GestorSesion.class.getSimpleName();
    private static final String KEY_MARKER_SELECCIONADO = "markerSeleccionado";
    private static GestorSesion singleton;
    private final EventoObservable onInicializado = new EventoObservable();
    //Markers activos
    private ArrayList<Marcador> marcadors;
    //Facebook token
    private AccessToken token;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private User me;
    private User[] friends;
    private EmisorMensajes emisor;
    private Marcador marcadorActivo;
    private SharedPreferences preferences;

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
        emisor = new EmisorMensajes();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        GraphRequest request;
        request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                me = new Gson().fromJson(jsonObject.toString(), User.class);
                getMarkersDB();
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
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    private void getMarkersDB() {
        firebaseDatabase.getReference("/usuarios/"+me.getId()+"/markers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        marcadors = new ArrayList<>();
                        String idMarker = preferences.getString(KEY_MARKER_SELECCIONADO, null);
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            try {
                                Marcador value = data.getValue(Marcador.class);
                                marcadors.add(value);
                                if (value.getId().equals(idMarker)) {
                                    marcadorActivo = value;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "No se pudo deserializar Marcador", e);
                            }
                        }
                        if (marcadorActivo == null) {
                            preferences.edit()
                                    .remove(KEY_MARKER_SELECCIONADO)
                                    .apply();
                        }
                        notificarInicializacion();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        marcadors = new ArrayList<>();
                        notificarInicializacion();
                    }
                });
    }

    private void notificarInicializacion() {
        if (inicializado()) {
            actualizarTokenEnServidor();
            onInicializado.notificar();
        }
    }

    public boolean inicializado() {
        return me != null && friends != null && marcadors != null;
    }

    public boolean loggeado() {
        return mAuth != null && token != null;
    }

    public ArrayList<Marcador> getMarcadores() {
        return marcadors;
    }

    /**
     * Crea un marker con mi propio usuario
     * @param destino destino del marker
     * @param radioDeteccion radio de deteccion del marker
     * @return marker creado
     */
    public Marcador crearMarcador(Destino destino, int radioDeteccion) {
        return crearMarcador(destino, radioDeteccion, new ArrayList<User>());
    }

    public Marcador crearMarcador(Destino destino, int radioDeteccion, ArrayList<User> contactsToShare) {
        Marcador marcador = new Marcador(me, destino, radioDeteccion);

        List<String> usuarios = marcador.getUsuarios();
        for (User user : contactsToShare) {
            usuarios.add(user.getId());
        }

        marcadors.add(marcador);
        DatabaseReference ref = firebaseDatabase
                .getReference("/usuarios/" + me.getId() + "/markers")
                .push();
        marcador.setId(ref.getKey());
        ref.setValue(marcador);

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

    public void eliminarMarcador(Marcador marcador) {
        if (marcadorActivo == marcador) {
            marcadorActivo = null;
        }
        //todo si no hay internet habra q persistir registros por sincronizar
        firebaseDatabase
                .getReference("/usuarios/"+me.getId()+"/markers/"+marcador.getId())
                .removeValue();
        marcadors.remove(marcador);
    }

    public void setMarcadorActivo(Marcador marcadorActivo) {
        this.marcadorActivo = marcadorActivo;
        String id = null;
        if (marcadorActivo != null) {
            id = marcadorActivo.getId();
        }
        preferences.edit()
                .putString(KEY_MARKER_SELECCIONADO, id)
                .apply();
    }

    public Marcador getMarcadorActivo() {
        return marcadorActivo;
    }
}