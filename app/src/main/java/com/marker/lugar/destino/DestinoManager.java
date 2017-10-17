package com.marker.lugar.destino;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marker.app.EventoObservable;
import com.marker.locator.LatLong;
import com.marker.lugar.history.History;

import java.util.ArrayList;

public class DestinoManager implements Parcelable{
    private static final String TAG = "Destino";
    private static DatabaseReference mDatabase;
    public  ArrayList<Destino> destinos = new ArrayList<>();
    private String userId;
    private EventoObservable onInicializado = new EventoObservable();

    public DestinoManager() {}

    public void inicializar(String userId){
        final ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Destino destino = dataSnapshot.getValue(Destino.class);
                if (!destinos.contains(destino)) {
                    destinos.add(destino);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                Destino destino = dataSnapshot.getValue(Destino.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                Destino removedDestino = dataSnapshot.getValue(Destino.class);

                // FML
                int position = 0;
                for (Destino lugar : destinos) {
                    if(lugar.uid.equals(removedDestino.uid))
                        break;
                    position += 1;
                }

                destinos.remove(position);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };

        this.userId = userId;

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final Query orderedQuery = mDatabase
                .child("usuarios")
                .child(userId)
                .child("destinos")
                .orderByChild("datetime");
        orderedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Destino destino = snapshot.getValue(Destino.class);
                    destinos.add(destino);
                }
                onInicializado.notificar();
                orderedQuery.addChildEventListener(childEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected DestinoManager(Parcel in) {
        destinos = in.createTypedArrayList(Destino.CREATOR);
        userId = in.readString();
    }

    public static final Creator<DestinoManager> CREATOR = new Creator<DestinoManager>() {
        @Override
        public DestinoManager createFromParcel(Parcel in) {
            return new DestinoManager(in);
        }

        @Override
        public DestinoManager[] newArray(int size) {
            return new DestinoManager[size];
        }
    };

    public void writePlace(Place place){
        LatLong latLong = new LatLong(place.getLatLng().latitude, place.getLatLng().longitude);
        this.writeLugar(place.getName().toString(), latLong);
    }

    public void writeLugar(String location, LatLong position) {
        String uid = mDatabase.child("usuarios").child(userId).child("destinos").push().getKey();
        Destino destino = new Destino(location, "", position);
        destino.uid = uid;
        mDatabase.child("usuarios").child(userId).child("destinos").child(uid).setValue(destino);
    }

    public void deleteLugar(String uid){
        mDatabase.child("usuarios").child(userId).child("destinos").child(uid).removeValue();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeList(this.destinos);
    }

    public EventoObservable getOnInicializado() {
        return onInicializado;
    }
}