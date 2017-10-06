package com.marker.lugar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.marker.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LugarActivity extends AppCompatActivity {

    @BindView(R.id.rv_lugares)
    RecyclerView rvLugares;

    private LugaresRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_lugares);
        ButterKnife.bind(this);
        adapter = new LugaresRecyclerViewAdapter();
        rvLugares.setAdapter(adapter);
        rvLugares.setLayoutManager(new LinearLayoutManager(this));

        List<Lugar> lugares = this.mockData();

        adapter.setItems(lugares);
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

    public void lugarSeleccionado(Intent data){
        setResult(RESULT_OK, data);
        finish();
    }

    public final List<Lugar> mockData(){
        List<Lugar> lugares = new ArrayList<>();

        lugares.add(new Lugar("Mi casa",
                "",
                new LatLng(-34.624799, -58.492793)));
        lugares.add(new Lugar("Cinemark Caballito",
                "",
                new LatLng(-34.616298, -58.428772)));
        lugares.add(new Lugar("Panadería",
                "",
                new LatLng(-34.607571, -58.374958)));
        lugares.add(new Lugar("Teatro",
                "",
                new LatLng(-34.601072, -58.382956)));
        lugares.add(new Lugar("Facultad",
                "",
                new LatLng(-34.598575, -58.420118)));
        lugares.add(new Lugar("Taller de Toto",
                "",
                new LatLng(-34.626755, -58.491268)));
        lugares.add(new Lugar("Laburo",
                "",
                new LatLng(-34.597357, -58.372061)));
        return lugares;
    }
}
