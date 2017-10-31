package com.marker.lugar.history;

import android.content.Intent;
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
import com.marker.lugar.destino.Destino;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryActivity extends AppCompatActivity {

    @BindView(R.id.rv_histories)
    RecyclerView rvHistories;

    @BindView(R.id.progress_overlay)
    protected View progress_overlay;

    private HistoryRecyclerViewAdapter adapter;

    private ArrayList<History> histories = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        adapter = new HistoryRecyclerViewAdapter();
        rvHistories.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvHistories.setLayoutManager(layoutManager);

        Bundle extras = getIntent().getExtras();

        if(savedInstanceState != null) {
            histories = savedInstanceState.getParcelableArrayList("histories");
            if(histories != null) {
                adapter.setItems(histories);
            } else {
                getFirebaseHistories();
            }
        } else {
            getFirebaseHistories();
        }
    }

    private void getFirebaseHistories() {
        progress_overlay.setVisibility(View.VISIBLE);
        HistoryManager historyManager = new HistoryManager(GestorSesion.getInstancia(this).getUsuarioLoggeado()) {
            @Override
            protected void onGetHistories(ArrayList<History> histories) {
                HistoryActivity.this.histories = histories;
                adapter.setItems(histories);
                progress_overlay.setVisibility(View.GONE);
            }
        };
        historyManager.requestHistories();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(histories != null) {
            outState.putParcelableArrayList("histories", histories);
        }
    }


}
