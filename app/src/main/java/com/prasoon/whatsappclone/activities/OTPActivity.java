package com.prasoon.whatsappclone.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.prasoon.whatsappclone.databinding.ActivityOtpactivityBinding;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    ActivityOtpactivityBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        binding.otpView.requestFocus();
        pd = new ProgressDialog(this);
        pd.setMessage("Sending OTP...");
        pd.setCancelable(false);
        pd.show();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneLabel.setText("Verify " + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        pd.dismiss();
                        InputMethodManager inputMethodManager =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(
                                binding.otpView.getApplicationWindowToken(),
                                InputMethodManager.SHOW_FORCED, 0);
                        verificationId = verifyId;
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(OTPActivity.this, SetupProfileActivity.class));
                            finishAffinity();
                        } else {
                            Toast.makeText(OTPActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}