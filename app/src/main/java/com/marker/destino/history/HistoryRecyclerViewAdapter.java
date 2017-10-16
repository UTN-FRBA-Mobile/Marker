package com.marker.destino.history;

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
import com.marker.destino.lugar.GuardarLugarFragment;
import com.marker.destino.lugar.LugarManager;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
            textViewHistory.setText(history.nombre);
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
            Activity parentActivity = (Activity) HistoryRecyclerViewAdapter.this.context;

            GuardarLugarFragment guardarLugarFragment = new GuardarLugarFragment();
            Bundle args = new Bundle();
            args.putParcelable("history", history);
            args.putParcelable("lugarManager", lugarManager);
            guardarLugarFragment.setArguments(args);
            guardarLugarFragment.show(parentActivity.getFragmentManager(), "HistoryActivity");

            return true;
        }
    }
}
