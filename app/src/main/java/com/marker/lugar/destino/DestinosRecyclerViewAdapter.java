package com.marker.lugar.destino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marker.R;
import com.marker.app.GestorSesion;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class DestinosRecyclerViewAdapter extends RecyclerView.Adapter<DestinosRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Destino> destinos = new ArrayList<>();
    private Context context;
    public DestinoManager destinoManager;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        destinoManager = new DestinoManager(GestorSesion.getInstancia().getUsuarioLoggeado().getId());
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_destinos_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return destinos.size();
    }

    public void setItems(ArrayList<Destino> items) {
        destinos = items;
        notifyDataSetChanged();
    }

    public void deleteDestino(Destino removedDestino) {
        int position = 0;
        for (Destino destino : destinos) {
            if(destino.uid.equals(removedDestino.uid))
                break;
            position += 1;
        }

        destinos.remove(position);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView card;

        @BindView(R.id.imageViewLugar)
        ImageView imageViewLugar;

        @BindView(R.id.txtLugar)
        TextView txtLugar;

        private Destino destino;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            destino = destinos.get(position);
            txtLugar.setText(destino.nombre);
        }

        @OnClick(R.id.card)
        void onClickCard() {
            Activity parentActivity = (Activity) DestinosRecyclerViewAdapter.this.context;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("destino", destino);
            parentActivity.setResult(Activity.RESULT_OK, resultIntent);
            parentActivity.finish();
        }

        @OnLongClick(R.id.card)
        boolean onLongClickCard(){
            Activity parentActivity = (Activity) DestinosRecyclerViewAdapter.this.context;
            BorrarDestinoFragment borrarDestinoFragment = new BorrarDestinoFragment();
            Bundle args = new Bundle();
            args.putParcelable("destino", destino);
            args.putParcelable("destinoManager", destinoManager);
            borrarDestinoFragment.setArguments(args);
            borrarDestinoFragment.show(parentActivity.getFragmentManager(), "DestinoActivity");
;
            return true;
        }
    }
}
