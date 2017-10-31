package com.marker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.marker.app.EventoObservable;
import com.marker.app.GestorSesion;
import com.marker.facebook.User;
import com.marker.menu.MenuEnum;

import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity{
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessToken token = AccessToken.getCurrentAccessToken();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || token == null) {
            mostrarLogin();
        } else {
            verifyPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MenuEnum.LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                verifyPermissions();
            } else {
                mostrarLogin();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    inicializarSesion();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    alertPermissions(false);
                }
                return;
            }
        }
    }

    protected void verifyPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                alertPermissions(true);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
        } else {
            inicializarSesion();
        }
    }

    protected void alertPermissions(final boolean retry) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Importante");
        dialog.setMessage("Es necesario aceptar los permisos para iniciar la aplicación");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                if(retry) {
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                    return;
                }
                finishAffinity();
            }
        });
        dialog.show();
    }

    protected void alertNoConnection() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Importante");
        dialog.setMessage("No se pudo conectar al servidor, verifique su conexión a internet");
        dialog.setCancelable(false);
        dialog.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                finishAffinity();
            }
        });
        dialog.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                inicializarSesion();
            }
        });
        dialog.show();
    }

    private void mostrarLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, MenuEnum.LOGIN_REQUEST);
    }

    private void inicializarSesion() {
        final GestorSesion gestorSesion = GestorSesion.getInstancia(this);

        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse response) {
                if(jsonObject == null) {
                    alertNoConnection();
                    return;
                }
                gestorSesion.setUsuarioLogueado(new Gson().fromJson(jsonObject.toString(), User.class));

                try {
                    gestorSesion.inicializar();
                    lanzarMain();
                } catch (Exception e) {
                    Log.e(TAG, "No se pudo inicializar sesion!", e);
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);

        request.executeAsync();
    }

    private void lanzarMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}