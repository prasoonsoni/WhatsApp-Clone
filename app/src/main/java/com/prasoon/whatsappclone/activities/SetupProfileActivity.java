package com.prasoon.whatsappclone.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prasoon.whatsappclone.databinding.ActivitySetupProfileBinding;
import com.prasoon.whatsappclone.models.User;

public class SetupProfileActivity extends AppCompatActivity {
    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        pd =  new ProgressDialog(this);
        pd.setMessage("Setting up your profile...");
        pd.setCancelable(false);

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,45);
        });

        binding.setupButton.setOnClickListener(v -> {
            String name = binding.nameBox.getText().toString();
            if(name.isEmpty()){
                binding.nameBox.setError("Please enter a name");
                return;
            }
            pd.show();
            if(selectedImage!=null){
                StorageReference reference = storage.getReference().child("profiles").child(auth.getUid());
                reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    String uid = auth.getUid();
                                    String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                                    String name = binding.nameBox.getText().toString();

                                    User user = new User(uid, name, phoneNumber, imageUrl);
                                    database.getReference()
                                            .child("users")
                                            .child(uid)
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    pd.dismiss();
                                                    startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                                                    finish();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                String uid = auth.getUid();
                String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                User user = new User(uid, name, phoneNumber, "No Image");
                database.getReference()
                        .child("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                                finish();
                            }
                        });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}