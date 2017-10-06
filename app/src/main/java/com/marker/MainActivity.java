package com.marker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.marker.contact.Contact;
import com.marker.contact.ContactActivity;
import com.marker.history.History;
import com.marker.history.HistoryActivity;
import com.marker.locator.Locator;
import com.marker.lugar.LugarActivity;
import com.marker.map.MarkerMap;
import com.marker.permission.Permission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    static final int PICK_HISTORY_REQUEST = 1;
    static final int PICK_CONTACT_REQUEST = 2;
    static final int PICK_LUGAR_REQUEST = 3;
    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 35;

    private static final String TAG = "MainActivity";

    public MarkerMap map;
    private ArrayList<Contact> trackedContacts = Contact.initializeData();
    public Permission permission = new Permission(this);
    private Locator locator;
    private GoogleApiClient mGoogleApiClient;
    // TODO: agregar una propiedad que sea el usuario trackeado con el marker
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.start_track);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Se inicia el marker", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                OnContactsPressed();
            }
        });
        fab.setEnabled(false);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorDisabled)));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initialize_geo();
    }

    private void initialize_geo() {
        this.mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        LocationRequest locationRequest = LocationRequest.create();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        this.map = new MarkerMap(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.locator = new Locator(this);
        this.locator.setClient(LocationServices.getFusedLocationProviderClient(this));
        this.locator.getLocation();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mOptionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_track) {
            return true;
        } else if (id == R.id.action_search) {
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }
        } else if(id == R.id.tracked_0){
            showSnackbar("Yo");
        } else if(id == 1) {
            showSnackbar("Contacto 1");
        } else if(id == 2) {
            showSnackbar("Contacto 2");
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_destinies) {
            OnDestiniesPressed();
        } else if (id == R.id.nav_histories) {
            OnHistoriesPressed();
        } else if (id == R.id.nav_settings) {
            OnSettingsPressed();
        } else if (id == R.id.nav_info) {
            OnAboutPressed();
        } else if (id == R.id.nav_test_notification) {
            OnTestNotificationPressed();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void OnDestiniesPressed() {
        startActivity(new Intent(this, LugarActivity.class));
    }

    public void OnHistoriesPressed() { startActivityForResult(new Intent(this, HistoryActivity.class), PICK_HISTORY_REQUEST); }

    public void OnContactsPressed() { startActivityForResult(new Intent(this, ContactActivity.class), PICK_CONTACT_REQUEST); }

    public void OnSettingsPressed() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public boolean OnAboutPressed(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Acerca");
        alertDialog.setMessage(
            "App para Desarrollo de aplicaciones móbiles." + "\n\n" +
            "Desarrollado por: " + "\n" +
            "Ezequiel Ayzenberg" + "\n" +
            "Fernando Velcic"  + "\n" +
            "Francisco Bravo"  + "\n" +
            "Sandro Damilano"  + "\n"
        );

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Continuar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
        return true;
    }


    public void OnTestNotificationPressed() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Uri notification = Uri.parse(sharedPreferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND"));
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};

        // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map.setMap(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case PICK_HISTORY_REQUEST:
                if(resultCode == RESULT_OK){
                    History history = data.getParcelableExtra("history");
                    this.map.setPosition(history.position);

                    enableTrackButton();

                    startActivityForResult(new Intent(this, ContactActivity.class), PICK_CONTACT_REQUEST);
                }
                break;
            case PICK_CONTACT_REQUEST:
                if(resultCode == RESULT_OK){
                    // Cuando se vuelve de PICK_CONTACT ya puedo iniciar el marker
                    Bundle extras = data.getExtras();
                    // Obtengo los contactos seleccionados para compartir mi marker
                    ArrayList<Contact> contactsToShare = extras.getParcelableArrayList("selectedContacts");
                    //FIXME: en un futuro el update del menu deberia ser con los contactos trackeados
                    updateTrackMenu(contactsToShare);
                    //TODO: Compartir el marker

                    // Por default el usuario va a ver su propio marker asi que obtenemos su posicion
                    this.locator.getLocation();
                    try {
                        this.map.centerCamera();
                    } catch (Exception e) {
                        showSnackbar("GPS is not on!");
                    }

                }
                break;
            case PICK_LUGAR_REQUEST:
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    map.setPosition(place.getLatLng());
                    map.updateCamera();

                    enableTrackButton();

                    Log.i(TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
        }
    }

    private void enableTrackButton() {
        FloatingActionButton fab = findViewById(R.id.start_track);
        fab.setEnabled(true);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryLight)));
    }

    private void updateTrackMenu(ArrayList<Contact> contactsToShare) {
        Integer i;
        for(i=0; i < contactsToShare.size(); i++){
            Contact contact = contactsToShare.get(i);
            mOptionsMenu.removeItem(i);
            mOptionsMenu.add(R.id.action_track, i, Menu.FLAG_APPEND_TO_GROUP, contact.name);
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.map);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    public void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public void startLocationPermissionRequest() {
        this.permission.startLocationPermissionRequest();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                this.locator.getLocationOnMap();
            } else {
                // Permission denied.
                showSnackbar(R.string.permission_denied_explanation, R.string.action_settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}
