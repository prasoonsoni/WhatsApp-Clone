package com.prasoon.whatsappclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prasoon.whatsappclone.R;
import com.prasoon.whatsappclone.adapters.MessagesAdapter;
import com.prasoon.whatsappclone.databinding.ActivityChatBinding;
import com.prasoon.whatsappclone.models.Message;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String imageUrl = getIntent().getStringExtra("image_url");
        senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.chatRecyclerView.setAdapter(adapter);


        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()){
                        if(status.equals("Offline")){
                            binding.status.setVisibility(View.GONE);
                        } else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                        binding.chatRecyclerView.scrollToPosition(messages.size()-1);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.sendButton.setOnClickListener(v -> {
            String messageText = binding.editTextTextPersonName.getText().toString();
            Date date = new Date();
            if(messageText.isEmpty()){
                binding.editTextTextPersonName.setError("Message cannot be empty!");
            } else {
                Message message = new Message(messageText, senderUid, date.getTime());

                String randomKey = database.getReference().push().getKey();
                HashMap<String,Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                binding.editTextTextPersonName.setText("");
                                InputMethodManager imm = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                }
                                imm.hideSoftInputFromWindow(binding.editTextTextPersonName.getWindowToken(), 0);
                                binding.chatRecyclerView.scrollToPosition(messages.size()-1);
                            }
                        });

                    }
                });
            }

        });

        binding.attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,25);
        });


        binding.name.setText(name);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.avatar)
                .into(binding.userImage);
        binding.backButton.setOnClickListener(v -> {
            onBackPressed();
        });
        final Handler handler = new Handler();
        binding.editTextTextPersonName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }
            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageText = binding.editTextTextPersonName.getText().toString();
                                        Date date = new Date();
                                            Message message = new Message(messageText, senderUid, date.getTime());
                                            message.setMessage("Photo");
                                            message.setImageUrl(filePath);
                                            String randomKey = database.getReference().push().getKey();
                                            HashMap<String,Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg",message.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                            database.getReference().child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            binding.chatRecyclerView.scrollToPosition(messages.size()-1);
                                                            binding.editTextTextPersonName.setText("");
                                                        }
                                                    });

                                                }
                                            });
                                        }


                                });
                            }
                        }
                    });
                }
            }
        }
    }
}