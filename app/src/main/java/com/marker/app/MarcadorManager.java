package com.marker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marker.facebook.User;
import com.marker.lugar.destino.Destino;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MarcadorManager implements ValueEventListener {
    private static final String TAG = MarcadorManager.class.getSimpleName();
    private static final String KEY_MARKER_SELECCIONADO = "markerSeleccionado";
    private static final String KEY_MARKERS = "markers";
    private EventoObservable onInicializado = new EventoObservable();
    private SharedPreferences preferences;
    private DatabaseReference refMarkers;
    private User usuarioLoggeado;

    public MarcadorManager(Context context) {
        preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        usuarioLoggeado = GestorSesion
                .getInstancia(context)
                .getUsuarioLoggeado();
        refMarkers = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("usuarios")
                .child(usuarioLoggeado.getId())
                .child("markers");
        refMarkers.addListenerForSingleValueEvent(this);
    }

    public ArrayList<Marcador> getMarcadores() {
        String markersStr = preferences.getString(KEY_MARKERS, "");
        if (markersStr.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Marcador>>(){}.getType();
        return new Gson().fromJson(markersStr, type);
    }

    public Marcador crearMarcador(Destino destino, int radioDeteccion, ArrayList<User> contactsToShare) {
        Marcador marcador = new Marcador(usuarioLoggeado,
                destino, radioDeteccion);

        List<String> usuarios = marcador.getUsuarios();
        for (User user : contactsToShare) {
            usuarios.add(user.getId());
        }

        DatabaseReference ref = refMarkers.push();
        marcador.setId(ref.getKey());
        ref.setValue(marcador);
        agregarMarker(marcador);

        return marcador;
    }

    public void agregarMarker(Marcador marker) {
        ArrayList<Marcador> marcadores = getMarcadores();
        marcadores.add(marker);
        preferences.edit()
                .putString(KEY_MARKERS, new Gson().toJson(marcadores))
                .apply();
    }

    public void eliminarMarcador(Marcador marcador) {
        String idActivo = preferences.getString(KEY_MARKER_SELECCIONADO, "");
        String id = marcador.getId();
        if (id.equals(idActivo)) {
            preferences.edit()
                    .remove(KEY_MARKER_SELECCIONADO)
                    .apply();
        }
        //todo si no hay internet habra q persistir registros por sincronizar
        refMarkers.child(id).removeValue();
        ArrayList<Marcador> markers = getMarcadores();
        markers.remove(marcador);
        preferences.edit()
                .putString(KEY_MARKERS, new Gson().toJson(markers))
                .apply();
    }

    public void setMarcadorActivo(Marcador marcador) {
        String id = null;
        if (marcador != null) {
            id = marcador.getId();
        }
        preferences.edit()
                .putString(KEY_MARKER_SELECCIONADO, id)
                .apply();
    }

    public Marcador getMarcadorActivo() {
        String idActivo = preferences.getString(KEY_MARKER_SELECCIONADO, "");
        if (idActivo.isEmpty()) {
            return null;
        }
        for (Marcador marcador : getMarcadores()) {
            if (marcador.getId().equals(idActivo)) {
                return marcador;
            }
        }
        return null;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        boolean activoExiste = false;
        String idActivo = preferences.getString(KEY_MARKER_SELECCIONADO, "");
        ArrayList<Marcador> marcadores = new ArrayList<>();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            try {
                Marcador value = data.getValue(Marcador.class);
                if (value.getId().equals(idActivo)) activoExiste = true;
                marcadores.add(value);
            } catch (Exception e) {
                Log.e(TAG, "No se pudo deserializar Marcador", e);
            }
        }
        if (!activoExiste) {
            preferences.edit()
                    .remove(KEY_MARKER_SELECCIONADO)
                    .apply();
        }
        preferences.edit()
                .putString(KEY_MARKERS, new Gson().toJson(marcadores))
                .apply();
        onInicializado.notificar();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    public EventoObservable getOnInicializado() {
        return onInicializado;
    }
}