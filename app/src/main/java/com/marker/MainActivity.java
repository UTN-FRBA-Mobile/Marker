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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.marker.lugar.Lugar;
import com.marker.lugar.destino.Destino;
import com.marker.facebook.User;
import com.marker.firebase.Mensaje;
import com.marker.friends.FriendsActivity;
import com.marker.lugar.history.History;
import com.marker.lugar.history.HistoryManager;
import com.marker.locator.LatLong;
import com.marker.locator.Locator;
import com.marker.lugar.destino.DestinoManager;
import com.marker.map.MarkerMap;
import com.marker.menu.MenuEnum;
import com.marker.menu.MenuFragment;
import com.marker.permission.Permission;
import com.marker.track.TrackListAdapter;

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
    @BindView(R.id.track_list)
    RecyclerView mTrackList;

    private MenuFragment menuFragment;

    public MarkerMap map;
    public Permission permission = new Permission(this);
    private Locator locator;
    private GoogleApiClient mGoogleApiClient;
    private Menu mOptionsMenu;
    public HistoryManager historyManager;
    public DestinoManager destinoManager;
    private GestorSesion gestorSesion;
    private List<BroadcastReceiver> receivers;
    private Lugar lugarActualSeleccionado;
    private TrackListAdapter mTrackListAdapter;
    private boolean mapReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        this.map = new MarkerMap(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        mTrackListAdapter = new TrackListAdapter();
        mTrackListAdapter.getOnCardAction().getObservers().add(new com.marker.track.EventoObservable.Observer() {
            @Override
            public void notificar(Marcador marker) {
                onTrackMenuMarkerClick(marker);
            }
        });
        mTrackList.setAdapter(mTrackListAdapter);
        mTrackList.setLayoutManager(new LinearLayoutManager(this));

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
                gestorSesion.inicializar(this);
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
        initialize_geo();
        historyManager = new HistoryManager(gestorSesion.getUsuarioLoggeado().getId());
        destinoManager = new DestinoManager(gestorSesion.getUsuarioLoggeado().getId());
        menuFragment.initializeManagers(historyManager, destinoManager);
        menuFragment.initializeFacebookUserData(gestorSesion.getUsuarioLoggeado());
        updateTrackMenu(gestorSesion.getMarcadores());
        if (mapReady) setMarcadorActivo(gestorSesion.getMarcadorActivo());

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


        getLocation();
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
        MenuItem item = menu.findItem(R.id.action_track);
        ArrayList<Marcador> marcadores = gestorSesion.getMarcadores();
        if (item != null) {
            if (marcadores != null) {
                item.setVisible(!marcadores.isEmpty());
            } else {
                item.setVisible(false);
            }
        }
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
                drawer.openDrawer(mTrackList);
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
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onTrackMenuMarkerClick(Marcador marker) {
        setMarcadorActivo(marker);
        //todo Centrar en el mapa el marker seleccionado
        drawer.closeDrawer(mTrackList);
    }

    @Override
    public void onMapReady(GoogleMap gmap) {
        map.setMap(gmap);
        mapReady = true;
        Marcador activo = gestorSesion.getMarcadorActivo();
        if (activo != null) {
            setMarcadorActivo(activo);
        }
    }

    private void mostrarMarcador(Marcador marker) {
        if (marker != null) {
            LatLong latLon = marker.getDestino().posicion;
            map.setPosition(new LatLng(latLon.latitude, latLon.longitude));
            centrarCamara();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        getLocation();
    }

    private void getLocation() {
        // FIXME: aca deberiamos preguntar si el marker activo es el nuestro u otro y obtener la ubicacion acorde
        try {
            this.locator.getLocation();
        } catch(NullPointerException e) {
            this.locator = new Locator(this);
            this.locator.setClient(LocationServices.getFusedLocationProviderClient(this));
            this.locator.getLocation();
        }
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
                    this.map.setPosition(new LatLng(history.posicion.latitude, history.posicion.longitude));

                    enableTrackButton(true);

                    lugarActualSeleccionado = history;

                    startActivityForResult(new Intent(this, FriendsActivity.class), MenuEnum.PICK_CONTACT_REQUEST);
                }
                break;
            case MenuEnum.PICK_CONTACT_REQUEST:
                if(resultCode == RESULT_OK){
                    // Cuando se vuelve de PICK_CONTACT ya puedo iniciar el marker

                    // Agrego el destino seleccionadoo al historial
                    // 2 casos:     1 - Fue seleccionado por búsqueda, historial o favoritos. Se deja como está.
                    //              2 - Fue seleccionado del mapa. Hay que actualizarlo porque está en null o marca otro lugar.
                    if(!map.markerPlacedOn(lugarActualSeleccionado)) {
                        lugarActualSeleccionado = map.getDestino();
                    }
                    historyManager.addPlace(lugarActualSeleccionado);

                    Bundle extras = data.getExtras();
                    // Obtengo los contactos seleccionados para compartir mi marker
                    ArrayList<User> contactsToShare = (ArrayList<User>) extras.getSerializable("selectedFriends");
                    Destino destino = new Destino();
                    destino.nombre = lugarActualSeleccionado.nombre;
                    destino.posicion = lugarActualSeleccionado.posicion;
                    Marcador marcador = gestorSesion
                            .crearMarcador(destino, 100, contactsToShare);

                    updateTrackMenu(gestorSesion.getMarcadores());
                    setMarcadorActivo(marcador);

                    // Por default el usuario va a ver su propio marker asi que obtenemos su posicion
                    getLocation();
                    centrarCamara();
                    this.map.activateFence();
                }
                break;
            case MenuEnum.PICK_DESTINO_REQUEST:
                if(resultCode == RESULT_OK) {
                    this.map.setRadio(getRadioSetting());

                    Destino destino = data.getParcelableExtra("destino");
                    this.map.setPosition(LatLong.toLatLng(destino.posicion));

                    enableTrackButton(true);

                    lugarActualSeleccionado = destino;

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
                    Destino destino = new Destino(place.getName().toString(), "", LatLong.of(place.getLatLng()));
                    map.setDestino(destino);

                    lugarActualSeleccionado = destino;
                    getLocation();

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

    private void centrarCamara() {
        try {
            this.map.centerCamera();
        } catch (Exception e) {
            showSnackbar("El GPS no esta prendido");
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
        mostrarMarcador(marcador);
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
        mTrackListAdapter.setItems(markers);
        invalidateOptionsMenu();
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