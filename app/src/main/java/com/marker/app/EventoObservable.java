package com.marker.app;

import java.util.ArrayList;

public class EventoObservable {
    private final ArrayList<ObserverSesion> observers = new ArrayList<>();

    public void notificar() {
        for (ObserverSesion observer : observers) {
            observer.notificar();
        }

    }

    public ArrayList<ObserverSesion> getObservers() {
        return observers;
    }

    public interface ObserverSesion {
        void notificar();
    }
}
