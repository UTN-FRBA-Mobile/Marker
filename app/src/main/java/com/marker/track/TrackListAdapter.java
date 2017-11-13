package com.marker.track;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.marker.R;
import com.marker.app.*;
import com.marker.facebook.User;
import com.marker.locator.LatLong;
import com.marker.lugar.destino.Destino;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder> {

    private ArrayList<Marcador> markers = new ArrayList<>();
    private static HashMap<String, Integer> colores = new HashMap<>();
    private Context context;
    private EventoObservable onCardAction = new EventoObservable();
    private EventoObservable onEliminarMarker = new EventoObservable();
    private String[] colorColeccion = { "#ff6e40", "#388e3c", "#039be5", "#039be5", "#8e24aa", "#e53935", "#8e24aa"};
    private HashMap<String, Integer> aEliminar = new HashMap<>();

    public TrackListAdapter(Context context) {
        MarcadorManager.getInstancia(context)
                .getOnMarkerEliminado()
                .getObservers()
                .add(new com.marker.app.EventoObservable.Observer() {
                    @Override
                    public void notificar(Marcador marker) {
                        markers.remove(marker);
                        notifyDataSetChanged();
                    }
                });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_track_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return markers.size();
    }

    public void setItems(ArrayList<Marcador> markers) {
        this.markers.clear();
        for (Marcador marker : markers) {
            this.markers.add(marker);
        }
        notifyDataSetChanged();
    }

    public EventoObservable getOnCardAction() {
        return onCardAction;
    }

    public EventoObservable getOnEliminarMarker() {
        return onEliminarMarker;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.friend_picture)
        ProfilePictureView mImgUsuario;
        @BindView(R.id.txt_nombre_usuario)
        TextView mTxtNombreUsuario;
        @BindView(R.id.txt_marker)
        TextView mTxtMarker;
        @BindView(R.id.card)
        CardView mCard;
        @BindView(R.id.android)
        ImageView mImgUsuarioEsquinas;

        private Marcador marker;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(int position) {
            marker = markers.get(position);
            setUsuario(marker.getUser());
            setLugar(marker.getDestino());
            setColor();
            controlarSeleccion();
        }

        private void controlarSeleccion() {
            Marcador marcadorActivo = MarcadorManager
                    .getInstancia(context)
                    .getMarcadorActivo();
            mCard.setAlpha(1);
            if (marcadorActivo == null) {
                return;
            }
            if (!marcadorActivo.equals(marker)) {
                mCard.setAlpha(.7f);
            }
        }

        private void setColor() {
            String id = marker.getId();
            Integer color = colores.get(id);
            if (color == null) {
                int idx = new Random().nextInt(colorColeccion.length);
                color = Color.parseColor(colorColeccion[idx]);
                colores.put(id, color);
            }
            mCard.setBackgroundColor(color);
            Drawable mIcon= ContextCompat.getDrawable(context, R.drawable.subtracted_circle_blank);
            mIcon.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            mImgUsuarioEsquinas.setImageDrawable(mIcon);
        }

        private void setUsuario(User user) {
            if (user == null) {
                mImgUsuario.setProfileId("");
                mTxtNombreUsuario.setText("Usuario no encontrado");
            }
            String id = user.getId();
            mImgUsuario.setProfileId(id);
            String name = user.getName();
            if (id.equals(GestorSesion.getInstancia(context).getUsuarioLoggeado().getId())) {
                name += " (Yo)";
            }
            mTxtNombreUsuario.setText(name);
        }

        private void setLugar(Destino destino) {
            if (destino == null) {
                mTxtMarker.setText("Sin destino especificado");
                return;
            }
            String nombreLugar = destino.nombre;
            if (nombreLugar != null) {
                mTxtMarker.setText(nombreLugar);
            } else {
                LatLong latLon = destino.posicion;
                mTxtMarker.setText(String.format("Lat: %s Long: %s",
                        latLon.latitude, latLon.longitude));
            }
        }

        @OnClick(R.id.card)
        void onCardClick() {
            MarcadorManager.getInstancia(context)
                    .setMarcadorActivo(marker);
            notifyDataSetChanged();
            onCardAction.notificar(marker);
        }

        @OnClick(R.id.btn_eliminar)
        void onEliminar() {
            eliminarMarker(marker);
        }
    }

    public void eliminarMarker(Marcador marker) {
        if (marker == null) {
            return;
        }
        onEliminarMarker.notificar(marker);
        aEliminar.put(marker.getId(), markers.indexOf(marker));
        markers.remove(marker);
        notifyDataSetChanged();
    }

    public void confirmarEliminacion(Marcador marker) {
        Integer pos = aEliminar.remove(marker.getId());
        if (pos == null) {
            return;
        }
        MarcadorManager.getInstancia(context)
                .eliminarMarcador(marker);
    }

    public void deshacerEliminacion(Marcador marker) {
        Integer pos = aEliminar.remove(marker.getId());
        if (pos == null) {
            return;
        }
        markers.add(pos, marker);
        notifyDataSetChanged();
    }

    public void confirmarEliminaciones() {
        Iterator<String> iterator = aEliminar.keySet().iterator();
        if (!iterator.hasNext()) {
            return;
        }
        Marcador marcador = MarcadorManager
                .getInstancia(context)
                .getMarcador(iterator.next());

        if (marcador == null) {
            return;
        }

        confirmarEliminacion(marcador);
    }
}
