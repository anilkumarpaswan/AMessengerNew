package com.piford.amessenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.piford.amessenger.ChatActivity;
import com.piford.amessenger.R;
import com.piford.amessenger.common.Common;
import com.piford.amessenger.models.User;

import java.util.ArrayList;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.MyHolder> {
    ArrayList<User> userList = new ArrayList<User> ( );
    Context context;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext ( );
        LayoutInflater inflater = LayoutInflater.from (context);
        View view = inflater.inflate (R.layout.list_items, parent, false);
        MyHolder holder = new MyHolder (view);
        return holder;
    }

    @Override
    public void onBindViewHolder (@NonNull MyHolder holder, int position) {
        final User user = userList.get (position);
        holder.tvName.setText (user.getName ( ));
        holder.tvPhone.setText (user.getPhone ( ));
        Glide.with (context).load (user.getProfilePic ( )).into (holder.imgPicture);


        holder.llContact.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                Intent intent = new Intent (Intent.ACTION_DIAL);
                intent.setData (Uri.parse ("tel:" + user.getPhone ( )));
                context.startActivity (intent);

            }
        });
    }

    @Override
    public int getItemCount () {
        return userList.size ( );
    }

    public void clear () {
        userList.clear ( );
        notifyDataSetChanged ( );
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView imgPicture;
        TextView tvName;
        TextView tvPhone;
        LinearLayout llContact;

        public MyHolder (@NonNull View itemView) {
            super (itemView);
            tvName = itemView.findViewById (R.id.tvName);
            tvPhone = itemView.findViewById (R.id.tvMobileNo);
            imgPicture = itemView.findViewById (R.id.imgPicture);
            llContact = itemView.findViewById (R.id.llContact);
        }
    }

    public void addItem (User item) {
        userList.add (item);
        notifyDataSetChanged ( );
    }

}
