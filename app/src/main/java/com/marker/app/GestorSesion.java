package com.marker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.marker.facebook.User;
import com.marker.firebase.EmisorMensajes;
import com.marker.firebase.Mensaje;

/**Singleton para gestionar lo que ocurre en la app
 */
public class GestorSesion {
    private static final String TAG = GestorSesion.class.getSimpleName();

    private static GestorSesion singleton;

    private FirebaseAuth mAuth;
    private User me;
    private SharedPreferences preferences;

    public static GestorSesion getInstancia(Context context){
        if (singleton == null) {
            singleton = new GestorSesion();
            singleton.setPreferences(PreferenceManager.getDefaultSharedPreferences(context));
        }
        return singleton;
    }

    private GestorSesion() {
    }

    /**Inicializa el usuario y amigos
     * @throws Exception Si se llama a este metodo sin estar loggeado
     */
    public void inicializar() throws Exception {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null) {
            throw new Exception("Debes loggearte antes de inicializar la sesion");
        }
    }

    public void actualizarTokenEnServidor() {
        User me = getUsuarioLoggeado();
        String uid = me.getId();
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase.getInstance()
                .getReference("usuarios/"+uid+"/token")
                .setValue(token);
    }

    public User getUsuarioLoggeado() {
        if(me == null) {
            return (new Gson()).fromJson(preferences.getString("loggedUser", ""), User.class);
        }
        return me;
    }

    public void setUsuarioLogueado(User user) {
        me = user;
        SharedPreferences.Editor sharedPreferencesEditor = preferences.edit();
        sharedPreferencesEditor.putString("loggedUser", (new Gson()).toJson(user));
        sharedPreferencesEditor.apply();
    }

    public void solicitarPosicion(User usuario) {
        Mensaje fcm = Mensaje.newDataMessage();
        fcm.setTipoData(Mensaje.TipoData.PEDIDO_POSICION);
        EmisorMensajes emisor = new EmisorMensajes();
        emisor.enviar(getUsuarioLoggeado(), usuario, fcm);
    }

    private void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }
}