package com.marker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.marker.facebook.User;
import com.marker.firebase.EmisorMensajes;
import com.marker.firebase.Mensaje;
import com.marker.lugar.destino.DestinoManager;

/**Singleton para gestionar lo que ocurre en la app
 */
public class GestorSesion {
    private static final String TAG = GestorSesion.class.getSimpleName();

    private static GestorSesion singleton;

    private final EventoObservable onInicializado = new EventoObservable();

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private User me;
    private SharedPreferences preferences;

    private DestinoManager destinoManager;
    private boolean destinosInicializado;

    private Context context;

    public static GestorSesion getInstancia(Context context){
        if (singleton == null) {
            singleton = new GestorSesion();
            singleton.context = context;
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
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        inicializarDestinosManager();

        setPreferences(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private void inicializarDestinosManager() {
        destinoManager = new DestinoManager();
        destinoManager.getOnInicializado().getObservers()
                .add(new EventoObservable.ObserverSesion() {
                    @Override
                    public void notificar() {
                        destinosInicializado = true;
                        notificarInicializacion();
                    }
                });
        destinoManager.inicializar(getUsuarioLoggeado().getId());
    }

    private void notificarInicializacion() {
        if (inicializado()) {
            actualizarTokenEnServidor();
            onInicializado.notificar();
        }
    }

    public boolean inicializado() {
        return getUsuarioLoggeado() != null
                 && destinosInicializado;
    }

    public void actualizarTokenEnServidor() {
        User me = getUsuarioLoggeado();
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

    public DestinoManager getDestinosManager() {
        return destinoManager;
    }

    public void solicitarPosicion(User usuario) {
        Mensaje fcm = Mensaje.newDataMessage();
        fcm.setTipoData(Mensaje.TipoData.PEDIDO_POSICION);
        EmisorMensajes emisor = new EmisorMensajes();
        emisor.enviar(getUsuarioLoggeado(), usuario, fcm);
    }

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }
}