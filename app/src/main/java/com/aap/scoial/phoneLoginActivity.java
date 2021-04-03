package com.aap.scoial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneLoginActivity extends AppCompatActivity {
    Button btnSendCode, btnVerify;
    EditText edtPhoneNo, edtOTPCode;
    ProgressDialog loadDialog;
    private String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mAuth = FirebaseAuth.getInstance();
        edtPhoneNo = findViewById(R.id.phnNoInput);
        edtOTPCode = findViewById(R.id.otpNoInput);
        btnSendCode = findViewById(R.id.btnSendVerfCode);
        btnVerify = findViewById(R.id.btnverifyfCode);
        loadDialog = new ProgressDialog(this);


        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendCode.setVisibility(View.INVISIBLE);
                edtPhoneNo.setVisibility(View.INVISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                edtOTPCode.setVisibility(View.VISIBLE);
                String phoneNo = edtPhoneNo.getText().toString();
                if (TextUtils.isEmpty(phoneNo)){
                    edtPhoneNo.setError("Enter Valid Phone No.");
                    edtPhoneNo.requestFocus();
                    return;
                }else {

                    loadDialog.setTitle("Phone Verification");
                    loadDialog.setMessage("Please wait while authenticating your phone");
                    loadDialog.setCanceledOnTouchOutside(false);
                    loadDialog.show();
//                    PhoneAuthOptions options =
//                            PhoneAuthOptions.newBuilder(mAuth)
//                                    .setPhoneNumber(phoneNo)       // Phone number to verify
//                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                                    .setActivity(phoneLoginActivity.this)                 // Activity (for callback binding)
//                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
//                                    .build();
//                    PhoneAuthProvider.verifyPhoneNumber(options);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNo,
                            60, TimeUnit.SECONDS, phoneLoginActivity.this, callbacks);
                }

            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadDialog.dismiss();
                Toast.makeText(phoneLoginActivity.this, "Invalid Phone number, Try with country code", Toast.LENGTH_SHORT).show();
                btnSendCode.setVisibility(View.VISIBLE);
                edtPhoneNo.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.INVISIBLE);
                edtOTPCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                loadDialog.dismiss();
                btnSendCode.setVisibility(View.INVISIBLE);
                edtPhoneNo.setVisibility(View.INVISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                edtOTPCode.setVisibility(View.VISIBLE);
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(phoneLoginActivity.this, "Code has been sent, Check your Message", Toast.LENGTH_SHORT).show();

            }
        };

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendCode.setVisibility(View.INVISIBLE);
                edtPhoneNo.setVisibility(View.INVISIBLE);
                String verificationCode = edtOTPCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)){
                    edtPhoneNo.setError("Enter Valid Code");
                    edtPhoneNo.requestFocus();
                    return;
                }else{
                    loadDialog.setTitle("Verifying Code");
                    loadDialog.setMessage("Please wait while verifying code...");
                    loadDialog.setCanceledOnTouchOutside(false);
                    loadDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadDialog.dismiss();
                            Toast.makeText(phoneLoginActivity.this, "Logged in with Phone No. successfully", Toast.LENGTH_SHORT).show();
                            senduserToMainActivity();
                        } else {
                            String msg = task.getException().toString();
                            Toast.makeText(phoneLoginActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void senduserToMainActivity() {
        Intent intent = new Intent(phoneLoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}