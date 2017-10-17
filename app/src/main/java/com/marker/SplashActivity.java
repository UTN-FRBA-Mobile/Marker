package com.marker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.marker.app.EventoObservable;
import com.marker.app.GestorSesion;
import com.marker.menu.MenuEnum;

public class SplashActivity extends AppCompatActivity implements EventoObservable.ObserverSesion{
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessToken token = AccessToken.getCurrentAccessToken();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || token == null) {
            mostrarLogin();
        } else {
            inicializarSesion();
        }
    }

    private void mostrarLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, MenuEnum.LOGIN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MenuEnum.LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                inicializarSesion();
            } else {
                mostrarLogin();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void inicializarSesion() {
        GestorSesion gestorSesion = GestorSesion.getInstancia();
        gestorSesion.getOnInicializado()
                .getObservers()
                .add(this);
        try {
            gestorSesion.inicializar(this);
        } catch (Exception e) {
            Log.wtf(TAG, "No se pudo inicializar sesion!!!", e);
        }
    }

    @Override
    public void notificar() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}