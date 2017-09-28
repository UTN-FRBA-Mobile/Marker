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

import com.marker.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactActivity extends AppCompatActivity {
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
}
