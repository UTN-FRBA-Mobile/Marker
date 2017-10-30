package com.marker.friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.marker.R;
import com.marker.facebook.User;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsActivity extends AppCompatActivity {
    @BindView(R.id.rv_friends)
    RecyclerView rvFriends;

    @BindView(R.id.progress_overlay)
    protected View progress_overlay;

    private FriendsRecyclerViewAdapter adapter;

    public ArrayList<User> selectedFriends = new ArrayList<>();
    private ArrayList<User> friends = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);
        adapter = new FriendsRecyclerViewAdapter();
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));


        if(savedInstanceState != null) {
            friends = savedInstanceState.getParcelableArrayList("friends");
            if(friends != null) {
                adapter.setItems(friends);
            } else {
                getFacebookFriends();
            }
        } else {
            getFacebookFriends();
        }
    }

    private void getFacebookFriends() {
        progress_overlay.setVisibility(View.VISIBLE);
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMyFriendsRequest(token, new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray objects, GraphResponse response) {
                if(objects == null) {
                    getFacebookFriends();
                    return;
                }
                User friends[] = new Gson().fromJson(objects.toString(), User[].class);
                FriendsActivity.this.friends = new ArrayList<>(Arrays.asList(friends));
                adapter.setItems(FriendsActivity.this.friends);
                progress_overlay.setVisibility(View.GONE);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);

        request.executeAsync();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(friends != null) {
            outState.putParcelableArrayList("friends", friends);
        }
    }
}
