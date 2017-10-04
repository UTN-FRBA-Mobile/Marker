package com.marker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenManager;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.marker.contact.FBUser;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InicioFBActivity extends AppCompatActivity implements FacebookCallback<LoginResult>, GraphRequest.Callback {

    @BindView(R.id.fb_login_button)
    protected LoginButton loginButton;
    @BindView(R.id.lista_amigos)
    protected TextView listaAmigos;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_fb);
        ButterKnife.bind(this);
        loginButton.setReadPermissions("email");
        loginButton.setReadPermissions("user_friends");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, this);
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            consultarAmigos(token);
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        AccessToken token = loginResult.getAccessToken();
        consultarAmigos(token);
    }

    private void consultarAmigos(AccessToken token) {
        new GraphRequest(token, "me/friends",
                null, HttpMethod.GET, this).executeAsync();
    }

    @Override
    public void onCancel() {
        // App code
    }

    @Override
    public void onError(FacebookException exception) {
        // App code
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCompleted(GraphResponse response) {
        try {
            JSONArray data = (JSONArray) response.getJSONObject().get("data");
            FBUser[] amigos = new Gson().fromJson(data.toString(), FBUser[].class);
            listaAmigos.setText("");
            for (FBUser amigo : amigos) {
                listaAmigos.append("\n"+amigo.getName());
            }
        } catch (JSONException e) {
            //No tiene campo data
            e.printStackTrace();
        }
    }
}
