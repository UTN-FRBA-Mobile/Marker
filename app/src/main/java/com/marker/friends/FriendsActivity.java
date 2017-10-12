package com.marker.friends;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.facebook.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsActivity extends AppCompatActivity {
    public ArrayList<User> selectedFriends = new ArrayList<>();

    @BindView(R.id.rv_friends)
    RecyclerView rvFriends;

    private FriendsRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);
        adapter = new FriendsRecyclerViewAdapter();
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        adapter.setItems(GestorSesion.getInstancia().getFriends());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_continue:
                Intent resultIntent = new Intent();
                Bundle extras = new Bundle();
                extras.putSerializable("selectedFriends",selectedFriends);
                resultIntent.putExtras(extras);
                this.setResult(Activity.RESULT_OK, resultIntent);
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
