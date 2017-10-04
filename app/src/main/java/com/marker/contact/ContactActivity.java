package com.marker.contact;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;
import com.marker.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactActivity extends AppCompatActivity implements GraphRequest.Callback {
    public ArrayList<Contact> selectedContacts = new ArrayList<>();

    @BindView(R.id.rv_contacts)
    RecyclerView rvContacs;

    private ContactRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);
        adapter = new ContactRecyclerViewAdapter();
        rvContacs.setAdapter(adapter);
        rvContacs.setLayoutManager(new LinearLayoutManager(this));

        List<Contact> contacts = Contact.initializeData();

        adapter.setItems(contacts);

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            new GraphRequest(token, "me/friends",
                    null, HttpMethod.GET, this).executeAsync();
        }
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
        inflater.inflate(R.menu.contact, menu);
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
                resultIntent.putParcelableArrayListExtra("selectedContacts", selectedContacts);
                this.setResult(Activity.RESULT_OK, resultIntent);
                this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompleted(GraphResponse response) {
        try {
            List<Contact> contacts = Contact.initializeData();
            JSONArray data = (JSONArray) response.getJSONObject().get("data");
            FBUser[] amigos = new Gson().fromJson(data.toString(), FBUser[].class);
            for (FBUser amigo : amigos) {
                contacts.add(new Contact(amigo.getName(), "", amigo.getEmail()));
            }
            adapter.setItems(contacts);
        } catch (JSONException e) {
            //No tiene campo data
            e.printStackTrace();
        }
    }
}
