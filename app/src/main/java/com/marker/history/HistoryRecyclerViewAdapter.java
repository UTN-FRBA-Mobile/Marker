package com.marker.history;

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

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<History> histories = new ArrayList<>();
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    }
}
