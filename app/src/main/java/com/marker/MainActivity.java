package com.marker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.google.android.gms.maps.model.LatLng;
import com.marker.app.EventoObservable;
import com.marker.app.GestorSesion;
import com.marker.app.Marcador;
import com.marker.facebook.User;
import com.marker.firebase.Mensaje;
import com.marker.friends.FriendsActivity;
import com.marker.history.History;
import com.marker.history.HistoryManager;
import com.marker.locator.LatLong;
import com.marker.locator.Locator;
import com.marker.lugar.Lugar;
import com.marker.lugar.LugarManager;
import com.marker.map.MarkerMap;
import com.marker.menu.MenuEnum;
import com.marker.menu.MenuFragment;
import com.marker.permission.Permission;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 35;

    private static final String TAG = "MainActivity";

    @BindView(R.id.nav_view)
    NavigationView mNavView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.start_track)
    FloatingActionButton fab;
    @BindView(R.id.stop_track)
    FloatingActionButton mStopTrack;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MenuFragment menuFragment;

    public MarkerMap map;
    public Permission permission = new Permission(this);
    private Locator locator;
    private GoogleApiClient mGoogleApiClient;
    private Menu mOptionsMenu;
    public HistoryManager historyManager;
    public LugarManager lugarManager;
    private GestorSesion gestorSesion;
    private List<BroadcastReceiver> receivers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Se inicia el marker", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivityForResult(new Intent(MainActivity.this, FriendsActivity.class), MenuEnum.PICK_CONTACT_REQUEST);
            }
        });
        enableTrackButton(false);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        menuFragment = (MenuFragment) getFragmentManager().findFragmentById(R.id.menu_fragment);
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                menuFragment.onNavigationItemSelected(item);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        gestorSesion = GestorSesion.getInstancia();
        final ArrayList<EventoObservable.ObserverSesion> observers = gestorSesion.getOnInicializado().getObservers();


        if (gestorSesion.inicializado()) {
            onSesionInicializada();
        } else {
            observers.add(new EventoObservable.ObserverSesion() {
                @Override
                public void notificar() {
                    onSesionInicializada();
                    observers.remove(this);
                }
            });
            try {
                gestorSesion.inicializar();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Integer action = intent.getIntExtra(getString(R.string.BROADCAST_ACTION),-1);
                switch (action) {
                    case R.string.BROADCAST_ACTION_NEW_MARKER:
                        updateTrackMenu(gestorSesion.getMarcadores());
                        break;
                    default:
                        break;
                }
            }
        };
        receivers = new ArrayList<>();
        receivers.add(receiver);
        registerReceiver(receiver,
                new IntentFilter(getString(R.string.BROADCAST_MARKER)));
    }

    @Override
    protected void onDestroy() {
        for (BroadcastReceiver receiver : receivers) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void onSesionInicializada() {
        historyManager = new HistoryManager(gestorSesion.getUsuarioLoggeado().getId());
        lugarManager = new LugarManager(gestorSesion.getUsuarioLoggeado().getId());
        menuFragment.initializeManagers(historyManager, lugarManager);
        menuFragment.initializeFacebookUserData(gestorSesion.getUsuarioLoggeado());
        initialize_geo();
        updateTrackMenu(gestorSesion.getMarcadores());

//        User emisor = gestorSesion.getUsuarioLoggeado();
//        Marcador marker = new Marcador(emisor, null, 100);
//        Mensaje fcm = Mensaje.newDataMessage();
//        fcm.setTipoData(Mensaje.TipoData.MARKER);
//        fcm.setMarker(marker);
//        gestorSesion.getEmisorMensajes().enviar(emisor, fcm);
    }

    public void generateNotification(Mensaje message) {
        GestorSesion gestorSesion = GestorSesion.getInstancia();
        gestorSesion.getEmisorMensajes()
                .enviar(gestorSesion.getUsuarioLoggeado(), message);
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
        updateTrackMenu(gestorSesion.getMarcadores());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_track:
                //todo Centrar en el mapa el marker seleccionado
                return true;
            case R.id.action_search:
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
            break;
            default:
                //Seleccion de Markers...
                Marcador marcador = gestorSesion.getMarcadores().get(id);
                setMarcadorActivo(marcador);
            break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onMapReady(GoogleMap map) {
        this.map.setMap(map);
    }

    private float getRadioSetting(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return (float) preferences.getInt("pr1", 200);
    }

    @OnClick(R.id.stop_track)
    public void onStopTrack() {
        Marcador marcador = gestorSesion.getMarcadorActivo();
        gestorSesion.eliminarMarcador(marcador);
        updateTrackMenu(gestorSesion.getMarcadores());

        fab.setVisibility(View.VISIBLE);
        enableTrackButton(false);
        mStopTrack.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case MenuEnum.PICK_HISTORY_REQUEST:
                if(resultCode == RESULT_OK){
                    this.map.setRadio(getRadioSetting());

                    History history = data.getParcelableExtra("history");
                    this.map.setPosition(new LatLng(history.position.latitude, history.position.longitude));

                    enableTrackButton(true);

                    startActivityForResult(new Intent(this, FriendsActivity.class), MenuEnum.PICK_CONTACT_REQUEST);
                }
                break;
            case MenuEnum.PICK_CONTACT_REQUEST:
                if(resultCode == RESULT_OK){
                    // Cuando se vuelve de PICK_CONTACT ya puedo iniciar el marker
                    Bundle extras = data.getExtras();
                    // Obtengo los contactos seleccionados para compartir mi marker
                    ArrayList<User> contactsToShare = (ArrayList<User>) extras.getSerializable("selectedFriends");
                    //FIXME: en un futuro el update del menu deberia ser con los contactos trackeados
                    Marcador marcador = gestorSesion
                            .crearMarcador(map.getLugar(), 100, contactsToShare);
                    updateTrackMenu(gestorSesion.getMarcadores());
                    //TODO: Compartir el marker

                    setMarcadorActivo(marcador);

                    // Por default el usuario va a ver su propio marker asi que obtenemos su posicion
                    this.locator.getLocation();
                    try {
                        this.map.centerCamera();
                    } catch (Exception e) {
                        showSnackbar("GPS is not on!");
                    }
                    this.map.activateFence();
                }
                break;
            case MenuEnum.PICK_LUGAR_REQUEST:
                if(resultCode == RESULT_OK) {
                    this.map.setRadio(getRadioSetting());

                    Lugar lugar = data.getParcelableExtra("lugar");
                    this.map.setPosition(LatLong.toLatLng(lugar.position));

                    enableTrackButton(true);

                    startActivityForResult(new Intent(this, FriendsActivity.class), MenuEnum.PICK_CONTACT_REQUEST);
                }
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    this.map.setRadio(getRadioSetting());

                    Place place = PlaceAutocomplete.getPlace(this, data);

                    map.setPosition(place.getLatLng());
                    map.updateCamera();
                    Lugar lugar = new Lugar(place.getName().toString(), "", LatLong.of(place.getLatLng()));
                    map.setLugar(lugar);

                    historyManager.writePlace(place);

                    enableTrackButton(true);

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

    private void setMarcadorActivo(Marcador marcador) {
        if (marcador == null) {
            fab.setVisibility(View.VISIBLE);
            enableTrackButton(false);
            mStopTrack.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.GONE);
            mStopTrack.setVisibility(View.VISIBLE);
            Snackbar.make(mStopTrack, marcador.getUser().getName(), 1000).show();
        }
        gestorSesion.setMarcadorActivo(marcador);
    }

    public void enableTrackButton(boolean enabled) {
        int color;
        if (enabled) {
            color = getResources().getColor(R.color.colorPrimaryLight);
        } else {
            color = getResources().getColor(R.color.colorDisabled);
        }
        fab.setBackgroundTintList(ColorStateList.valueOf(color));
        fab.setEnabled(enabled);
    }

    private void updateTrackMenu(ArrayList<Marcador> markers) {
        if(mOptionsMenu == null || markers == null) return;

        mOptionsMenu.clear();
        MenuItem search = mOptionsMenu.add(Menu.NONE, R.id.action_search,
                Menu.FIRST, "search");
        search.setIcon(R.drawable.ic_search);
        search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        for(int i=0; i < markers.size(); i++){
            User user = markers.get(i).getUser();
            mOptionsMenu.add(R.id.action_track, i,
                    Menu.FLAG_APPEND_TO_GROUP, user.getName());
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    public void showSnackbar(final String text) {
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