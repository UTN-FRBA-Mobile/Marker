package com.marker.lugar.destino;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.marker.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DestinoActivity extends AppCompatActivity {

    private ArrayList<Destino> destinos;

    @BindView(R.id.rv_destinos)
    RecyclerView rvDestinos;

    private DestinosRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_destinos);
        ButterKnife.bind(this);
        adapter = new DestinosRecyclerViewAdapter();
        rvDestinos.setAdapter(adapter);
        rvDestinos.setLayoutManager(new LinearLayoutManager(this));

        Bundle extras = getIntent().getExtras();

        if(savedInstanceState != null) {
            extras = savedInstanceState;
        }

        destinos = extras.getParcelableArrayList("destinos");

        adapter.setItems(destinos);
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
        outState.putParcelableArrayList("destinos", destinos);
    }
}
