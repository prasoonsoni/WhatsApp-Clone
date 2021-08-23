package com.prasoon.whatsappclone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.prasoon.whatsappclone.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {
    ActivityPhoneNumberBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.phoneBox.requestFocus();

        binding.continueButton.setOnClickListener(v -> {
            if(binding.phoneBox.getText().toString().isEmpty()){
                binding.phoneBox.setError("Field cannot be empty");
            } else {
                Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                intent.putExtra("phoneNumber", binding.phoneBox.getText().toString());
                startActivity(intent);
            }
        });
    }
}