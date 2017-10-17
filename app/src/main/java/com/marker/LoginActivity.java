package com.marker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements FacebookCallback<LoginResult>,
    OnCompleteListener<AuthResult>{

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.fb_login_button)
    protected Button loginButton;

    @BindView(R.id.progress_overlay)
    protected View progress_overlay;


    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress_overlay.setVisibility(View.VISIBLE);
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "user_friends"));
            }
        });

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);

    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        //Facebook OK

        AccessToken token = loginResult
                .getAccessToken();
        AuthCredential credential = FacebookAuthProvider
                .getCredential(token.getToken());
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, this);

        Log.d(TAG, "handleFacebookAccessToken:" + token);
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        progress_overlay.setVisibility(View.GONE);
        if (task.isSuccessful()) {
            //Firebase OK

            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success");

//            Intent intent = new Intent(this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
            setResult(RESULT_OK);
        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "signInWithCredential:failure", task.getException());
            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
            setResult(-1);
        }
        onBackPressed();
    }

    @Override
    public void onCancel() {
        setResult(-1);
        onBackPressed();
    }

    @Override
    public void onError(FacebookException error) {
        setResult(-1);
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}