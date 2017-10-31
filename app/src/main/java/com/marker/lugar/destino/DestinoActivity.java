package com.marker.lugar.destino;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.marker.R;
import com.marker.app.GestorSesion;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DestinoActivity extends AppCompatActivity {

    @BindView(R.id.rv_destinos)
    RecyclerView rvDestinos;

    @BindView(R.id.progress_overlay)
    protected View progress_overlay;

    private DestinosRecyclerViewAdapter adapter;

    private ArrayList<Destino> destinos = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_destinos);
        ButterKnife.bind(this);
        adapter = new DestinosRecyclerViewAdapter();
        rvDestinos.setAdapter(adapter);
        rvDestinos.setLayoutManager(new LinearLayoutManager(this));

        if(savedInstanceState != null) {
            destinos = savedInstanceState.getParcelableArrayList("destinos");
            if(destinos != null) {
                adapter.setItems(destinos);
            } else {
                getFirebaseDestinos();
            }
        } else {
            getFirebaseDestinos();
        }
    }


    private void getFirebaseDestinos() {
        progress_overlay.setVisibility(View.VISIBLE);
        DestinoManager destinoManager = new DestinoManager(GestorSesion.getInstancia(this).getUsuarioLoggeado()) {
            @Override
            protected void onGetDestinos(ArrayList<Destino> destinos) {
                DestinoActivity.this.destinos = destinos;
                adapter.setItems(destinos);
                progress_overlay.setVisibility(View.GONE);
            }
        };
        destinoManager.requestDestinos();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public DestinosRecyclerViewAdapter getAdapter(){
        return adapter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(destinos != null) {
            outState.putParcelableArrayList("destinos", destinos);
        }
    }
}
