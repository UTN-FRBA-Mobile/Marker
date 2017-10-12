package com.marker.lugar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marker.R;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LugaresRecyclerViewAdapter extends RecyclerView.Adapter<LugaresRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<Lugar> lugares = new ArrayList<>();
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_lugares_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }

    public void setItems(Collection<Lugar> items) {
        lugares.clear();
        lugares.addAll(items);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView card;

        @BindView(R.id.imageViewLugar)
        ImageView imageViewLugar;

        @BindView(R.id.txtLugar)
        TextView txtLugar;

        private Lugar lugar;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            lugar = lugares.get(position);
            txtLugar.setText(lugar.nombre);
        }

        @OnClick(R.id.card)
        void onClickCard() {
            Activity parentActivity = (Activity) LugaresRecyclerViewAdapter.this.context;
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lugar", lugar);
            parentActivity.setResult(Activity.RESULT_OK, resultIntent);
            parentActivity.finish();
        }
    }
}
