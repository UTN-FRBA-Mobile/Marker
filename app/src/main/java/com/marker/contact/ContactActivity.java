package com.marker.contact;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.marker.R;

public class ContactActivity extends AppCompatActivity implements ContactFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.contact_list, new ContactFragment())
                .commit();
    }

    public void onListFragmentInteraction(Contact contact){

    };
}
