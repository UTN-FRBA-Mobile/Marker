package com.marker.track;

import com.marker.app.Marcador;

import java.util.ArrayList;

public class EventoObservable {
    private final ArrayList<Observer> observers = new ArrayList<>();

    public void notificar(Marcador marker) {
        for (Observer observer : observers) {
            observer.notificar(marker);
        }

    }

    public ArrayList<Observer> getObservers() {
        return observers;
    }

    public interface Observer {
        void notificar(Marcador marker);
    }
}
