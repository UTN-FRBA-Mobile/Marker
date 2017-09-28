package com.marker.contact;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marker.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_contacts_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setItems(Collection<Contact> items) {
        contacts.clear();
        contacts.addAll(items);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView card;

        @BindView(R.id.imageViewContact)
        ImageView imageViewContact;

        @BindView(R.id.textContact)
        TextView textViewContact;

        @BindView(R.id.checkContact)
        CheckBox checkContact;

        private Contact contact;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            contact = contacts.get(position);
            textViewContact.setText(contact.name);
        }

        @OnClick(R.id.card)
        void onClickCard() {
            Toast.makeText(context, contact.name, Toast.LENGTH_SHORT).show();
        }

        @OnClick(R.id.checkContact)
        void onClickCheck() {
            this.contact.checked = true;
            ContactActivity parentActivity = (ContactActivity) ContactRecyclerViewAdapter.this.context;
            parentActivity.selectedContacts.add(this.contact);
        }
    }
}
