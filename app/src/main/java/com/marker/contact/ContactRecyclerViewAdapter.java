package com.marker.contact;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marker.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
 * specified {@link ContactFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> contacts;
    private final ContactFragment.OnListFragmentInteractionListener mListener;

    public ContactRecyclerViewAdapter(List<Contact> someContacts, ContactFragment.OnListFragmentInteractionListener listener) {
        contacts = someContacts;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.contact = contacts.get(position);
        holder.contactName.setText(contacts.get(position).name);
        holder.contactEmail.setText(contacts.get(position).email);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.contact);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public CardView contactView;
        TextView contactName;
        TextView contactEmail;
        public Contact contact;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            contactView = (CardView)view.findViewById(R.id.contact_view);
            contactName = (TextView)view.findViewById(R.id.contact_name);
            contactEmail = (TextView)view.findViewById(R.id.contact_email);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contactName.getText() + "'";
        }
    }
}
