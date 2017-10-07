package com.marker.app;

import com.marker.contact.Contact;
import com.marker.lugar.Lugar;

import java.util.ArrayList;

/**Singleton para gestionar lo que ocurre en la app
 */
public class GestorMarcadores {
    private static GestorMarcadores singleton = null;

    //Markers activos
    private final ArrayList<Marcador> marcadors = new ArrayList<>();
    private final Contact usuario;

    public static GestorMarcadores getInstancia() {
        if (singleton == null) {
            singleton = new GestorMarcadores();
        }
        return singleton;
    }

    private GestorMarcadores() {
        //todo traerlo de facebook...
        usuario = new Contact("yo", "", "");
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
        Marcador marcador = new Marcador(usuario, lugar, radioDeteccion);

        //todo controlar que no haya otro marcador que me trakee a mi mismo.
        marcadors.add(marcador);

        return marcador;
    }
}
