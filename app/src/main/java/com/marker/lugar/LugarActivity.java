package com.marker.lugar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.marker.locator.LatLong;
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

        Bundle extras = getIntent().getExtras();
        ArrayList<Lugar> lugares = extras.getParcelableArrayList("lugares");

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
}
