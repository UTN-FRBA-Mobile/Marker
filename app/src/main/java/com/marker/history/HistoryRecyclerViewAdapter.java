package com.marker.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.marker.R;
import com.marker.app.GestorSesion;
import com.marker.lugar.Lugar;
import com.marker.lugar.LugarManager;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<History> histories = new ArrayList<>();
    private Context context;
    public LugarManager lugarManager;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        lugarManager = new LugarManager(GestorSesion.getInstancia().getUsuarioLoggeado().getId());
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_histories_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    public void setItems(Collection<History> items) {
        histories.clear();
        histories.addAll(items);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView card;

        @BindView(R.id.imageViewHistory)
        ImageView imageViewHistory;

        @BindView(R.id.textHistory)
        TextView textViewHistory;

        private History history;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            history = histories.get(position);
            textViewHistory.setText(history.location);
        }

        @OnClick(R.id.card)
        void onClickCard() {
            Activity parentActivity = (Activity) HistoryRecyclerViewAdapter.this.context;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("history", history);
            parentActivity.setResult(Activity.RESULT_OK, resultIntent);
            parentActivity.finish();
        }

        @OnLongClick(R.id.card)
        boolean onLongClickCard(){
            final EditText nombreDestino = new EditText(context);

            AlertDialog.Builder saveDialog = new AlertDialog.Builder(HistoryRecyclerViewAdapter.this.context)
                .setTitle(String.format("¿Guardar %s en \"Mis Destinos\"?", history.location))
                .setMessage("Puede cambiarle el nombre:")
                .setView(nombreDestino)
                .setCancelable(true)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface saveDialog, int id) {
                        guardarDestino(history, nombreDestino.getText());
                    }
            });
            saveDialog.show();

            return true;
        }
    }

    private void guardarDestino(History history, Editable nombreDestino) {
        String nombre = TextUtils.isEmpty(nombreDestino) ? history.location : nombreDestino.toString();
        this.lugarManager.writeLugar(nombre, history.position);
    }
}
