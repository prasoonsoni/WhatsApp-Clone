package com.prasoon.whatsappclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prasoon.whatsappclone.activities.ChatActivity;
import com.prasoon.whatsappclone.R;
import com.prasoon.whatsappclone.models.User;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.MyViewHolder holder, int position) {
        User user = users.get(position);
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();
        holder.username.setText(user.getName());

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.messageTime.setText(dateFormat.format(new Date(time)));
                            holder.lastMessage.setText(lastMsg);
                        } else {
                            holder.lastMessage.setText("Tap to chat");
                            holder.messageTime.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.userImage);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("uid", user.getUid());
            intent.putExtra("image_url", user.getProfileImage());
            context.startActivity(intent);

        });
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username, lastMessage, messageTime;
        ImageView userImage;
        public MyViewHolder(@NonNull  View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            messageTime = itemView.findViewById(R.id.messageTime);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}
