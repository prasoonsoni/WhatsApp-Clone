package com.prasoon.whatsappclone.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prasoon.whatsappclone.R;
import com.prasoon.whatsappclone.databinding.ItemStatusBinding;
import com.prasoon.whatsappclone.models.Status;
import com.prasoon.whatsappclone.models.UserStatus;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.MyViewHolder>{

    Context context;
    ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {

        UserStatus userStatus = userStatuses.get(position);
        Log.i("links",userStatus.getStatuses().toString());

        long time = userStatus.getLastUpdated();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size()-1);
        Glide.with(context).load(lastStatus.getImageUrl()).into(holder.binding.circleImageView);
        holder.binding.userName.setText(userStatus.getName());
        holder.binding.lastUpdated.setText("Last Updated : "+ dateFormat.format(new Date(time)));
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());
        holder.binding.circularStatusView.setOnClickListener(v -> {
            ArrayList<MyStory> myStories = new ArrayList<>();
            for(Status status : userStatus.getStatuses()){
                myStories.add(new MyStory(status.getImageUrl()));
                Log.i("links", status.getImageUrl());
            }
            new StoryView.Builder(((AppCompatActivity)context).getSupportFragmentManager())
                    .setStoriesList(myStories) // Required
                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                    .setTitleText(userStatus.getName()) // Default is Hidden
                    .setSubtitleText("") // Default is Hidden
                    .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                    .setStoryClickListeners(new StoryClickListeners() {
                        @Override
                        public void onDescriptionClickListener(int position) {
                            //your action
                        }

                        @Override
                        public void onTitleIconClickListener(int position) {
                            //your action
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemStatusBinding binding;
        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }
}
