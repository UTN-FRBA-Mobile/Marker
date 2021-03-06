package com.marker.friends;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.marker.R;
import com.marker.facebook.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.ViewHolder> {
    private ArrayList<User> friends = new ArrayList<>();
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_friends_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setItems(ArrayList<User> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        CardView card;

        @BindView(R.id.friend_picture)
        ProfilePictureView friendProfilePicture;

        @BindView(R.id.friend_name)
        TextView friendNameTextView;

        @BindView(R.id.friend_check)
        CheckBox friendCheck;

        private User friend;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            friend = friends.get(position);
            friendNameTextView.setText(friend.getName());
            friendProfilePicture.setProfileId(friend.getId());
        }

        @OnClick(R.id.card)
        void onClickCard() {
            friendCheck.performClick();
            onClickCheck();
        }

        @OnClick(R.id.friend_check)
        void onClickCheck() {
            FriendsActivity parentActivity = (FriendsActivity) FriendsRecyclerViewAdapter.this.context;
            if(friendCheck.isChecked()) {
                parentActivity.selectedFriends.add(this.friend);
            } else {
                parentActivity.selectedFriends.remove(this.friend);
            }

        }
    }
}
