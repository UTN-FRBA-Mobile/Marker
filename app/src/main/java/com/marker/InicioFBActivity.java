package com.marker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.marker.facebook.FBUser;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InicioFBActivity extends AppCompatActivity implements FacebookCallback<LoginResult>, GraphRequest.Callback {

    private static final String TAG = "InicioFBActivity";
    @BindView(R.id.fb_login_button)
    protected LoginButton loginButton;

    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_fb);
        ButterKnife.bind(this);
        loginButton.setReadPermissions("email,user_friends");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            AccessToken token = AccessToken.getCurrentAccessToken();
            if (token != null) {
                handleFacebookAccessToken(token);
            }
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        handleFacebookAccessToken(loginResult.getAccessToken());
        consultarAmigos();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            consultarAmigos();
                            persistirInfo();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(InicioFBActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void persistirInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refUsuario = database.getReference("usuarios/"+user.getUid());
        refUsuario.child("nodoPrueba").setValue("Hola mundo!");
    }

    private void consultarAmigos() {
        AccessToken token = AccessToken.getCurrentAccessToken();
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
            //Obtengo amigos..
            JSONArray data = (JSONArray) response.getJSONObject().get("data");
            FBUser[] amigos = new Gson().fromJson(data.toString(), FBUser[].class);

            //Persisto lo obtenido en Firebase
            FirebaseUser user = mAuth.getCurrentUser();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference refUsuario = database.getReference("usuarios/"+user.getUid());

            if (amigos.length > 0) {
                refUsuario.child("amigos").setValue(data);
            }

            //Persisto un holamundo :D
            refUsuario.child("nodoPrueba").setValue("Hola mundo!");

        } catch (JSONException e) {
            //No tiene campo data
            e.printStackTrace();
        }
    }
}
