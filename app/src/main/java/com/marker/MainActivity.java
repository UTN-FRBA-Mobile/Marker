package com.marker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.marker.app.EventoObservable;
import com.marker.app.GestorSesion;
import com.marker.app.Marcador;
import com.marker.facebook.User;
import com.marker.friends.FriendsActivity;
import com.marker.locator.LatLong;
import com.marker.locator.Locator;
import com.marker.locator.LocatorService;
import com.marker.lugar.Lugar;
import com.marker.lugar.destino.Destino;
import com.marker.lugar.history.History;
import com.marker.lugar.history.HistoryManager;
import com.marker.map.MarkerMap;
import com.marker.menu.MenuEnum;
import com.marker.menu.MenuFragment;
import com.marker.track.TrackListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 35;
    static final int GPS_ENABLE_REQUEST = 40;

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
    private GestorSesion gestorSesion;
    private List<BroadcastReceiver> receivers;
    private Lugar lugarActualSeleccionado;
    private TrackListAdapter mTrackListAdapter;
    private boolean mapReady;
    private HashMap<String, LatLng> posiciones = new HashMap<>();
    private String usuarioActivoId;

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
        showTrackButton(false);


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

        gestorSesion = GestorSesion.getInstancia(this);
        final ArrayList<EventoObservable.ObserverSesion> observers = gestorSesion.getOnInicializado().getObservers();

        onSesionInicializada();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Integer action = intent.getIntExtra(getString(R.string.BROADCAST_ACTION),-1);
                switch (action) {
                    case R.string.BROADCAST_ACTION_NEW_MARKER:
                        updateTrackMenu(gestorSesion.getMarcadores());
                        break;
                    case R.string.BROADCAST_ACTION_POSITION:
                        LatLng posicion = intent.getParcelableExtra("posicion");
                        String uid = intent.getStringExtra("usuario");
                        posiciones.put(uid, posicion);
                        if (usuarioActivoId != null &&
                            usuarioActivoId.equals(uid)) {
                            mostrarPosicion(uid);
                        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if(this.map != null){
            this.map.setRadio(getRadioSetting());
            mostrarPosicionPropia();
        }
        getStoredLocation();
    }

    private LatLng getStoredLocation(){
        SharedPreferences mPrefs = getSharedPreferences("marker", MODE_PRIVATE);
        String json = mPrefs.getString("location", "");
        return new Gson().fromJson(json, LatLng.class);
    }

    private void onSesionInicializada() {
        initialize_geo();
        menuFragment.initializeManagers(gestorSesion.getDestinosManager());
        updateTrackMenu(gestorSesion.getMarcadores());
        if (mapReady) setMarcadorActivo(gestorSesion.getMarcadorActivo());
    }

    private void initialize_geo() {
        LocationRequest locationRequest = LocationRequest.create();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        startService(new Intent(this, LocatorService.class));
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
        User user = marker.getUser();
        if (gestorSesion.getUsuarioLoggeado().equals(user)) {
            mostrarPosicionPropia();
        } else {
            gestorSesion.solicitarPosicion(user);
            mostrarPosicion(user.getId());
        }
        drawer.closeDrawer(mTrackList);
    }

    private void mostrarPosicion(String id) {
        usuarioActivoId = id;
        LatLng latLng = posiciones.get(id);
        if (latLng == null) {
            map.deleterMarkerUser();
            map.centerCamera();
            return;
        }
        map.setUserPosition(latLng);
        map.centerCamera();
    }

    private void mostrarPosicionPropia() {
        Locator locator = new Locator(this);
        locator.getLocation(new Locator.ResultadoListener() {
            @Override
            public void onResultado(LatLng latLng) {
                map.setUserPosition(latLng);
                map.centerCamera();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap gmap) {
        map.setMap(gmap);
        mapReady = true;
        mostrarPosicionPropia();
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
        showTrackButton(false);
        mStopTrack.setVisibility(View.GONE);

        map.deleteMarker();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case MenuEnum.PICK_HISTORY_REQUEST:
                if(resultCode == RESULT_OK){
                    History history = data.getParcelableExtra("history");
                    this.map.setPosition(new LatLng(history.posicion.latitude, history.posicion.longitude));

                    showTrackButton(true);

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
                    HistoryManager historyManager = new HistoryManager(GestorSesion.getInstancia(this).getUsuarioLoggeado());
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
                    mostrarPosicionPropia();
                    this.map.activateFence();
                }
                break;
            case MenuEnum.PICK_DESTINO_REQUEST:
                if(resultCode == RESULT_OK) {
                    Destino destino = data.getParcelableExtra("destino");
                    this.map.setPosition(LatLong.toLatLng(destino.posicion));

                    showTrackButton(true);

                    lugarActualSeleccionado = destino;

                    showTrackButton(true);

                    startActivityForResult(new Intent(this, FriendsActivity.class), MenuEnum.PICK_CONTACT_REQUEST);
                }
                break;
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Place place = PlaceAutocomplete.getPlace(this, data);

                    map.setPosition(place.getLatLng());
                    map.updateCamera();
                    Destino destino = new Destino(place.getName().toString(), "", LatLong.of(place.getLatLng()));
                    map.setDestino(destino);

                    lugarActualSeleccionado = destino;
                    mostrarPosicionPropia();

                    showTrackButton(true);

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
            showTrackButton(false);
            mStopTrack.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.GONE);
            mStopTrack.setVisibility(View.VISIBLE);
            Snackbar.make(mStopTrack, marcador.getUser().getName(), 1000).show();
        }
        gestorSesion.setMarcadorActivo(marcador);
        mostrarMarcador(marcador);
    }

    public void showTrackButton(boolean enabled) {
        if (enabled) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setEnabled(enabled);
    }

    private void updateTrackMenu(ArrayList<Marcador> markers) {
        mTrackListAdapter.setItems(markers);
        invalidateOptionsMenu();
    }


    public void showSnackbar(final String text) {
        View container = findViewById(R.id.map);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    public void showGPSDiabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Deshabilitado");
        builder.setMessage("El GPS no esta encendido, por lo que no podra utilizar todas las funcionalidades de la aplicacion");
        builder.setPositiveButton("Activar GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
            }
        }).setNegativeButton("No, solo salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }
}