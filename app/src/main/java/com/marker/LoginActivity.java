package com.marker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btn_submit)
    Button btnSubmit;

    @BindView(R.id.txt_usuario)
    EditText txtUsuario;

    @BindView(R.id.txt_password)
    EditText txtPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_submit)
    public void onClickSubmit() {
        Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show();
    }
}
