package com.aap.scoial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class registerActivity extends AppCompatActivity {
    Button btnCreate;
    EditText edtEmail, edtPass, edtName;
    TextView textHaveAcc;
    FirebaseAuth mAuth;
    DatabaseReference mdatabase, codeRef;
    ProgressBar progressBar;
    ProgressDialog mdialogue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressBar =(ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        codeRef = FirebaseDatabase.getInstance().getReference("Invite");

        InitializeFeilds();
        textHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(registerActivity.this,loginActivity.class));
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRegister();
            }
        });
    }

    private void UserRegister() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            edtEmail.setError("Enter Valid Email");
            edtEmail.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError("Enter Valid Email");
            edtEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)){
            edtPass.setError("Enter Valid Password");
            edtPass.requestFocus();
            return;
        }else if (password.length()<6){
            edtPass.setError("Enter more than 6 character");
            edtPass.requestFocus();
            return;
        }

        mdialogue.setTitle("Creating New Account...");
        mdialogue.setMessage("Just a moment, We're setting up things for you...");
        mdialogue.setCanceledOnTouchOutside(true);
        mdialogue.show();
        codeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String setUsername = edtName.getText().toString();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currUserId = mAuth.getCurrentUser().getUid();
                                mdatabase.child("Users").child(currUserId).setValue("");
                                HashMap<String, Object> profileMap = new HashMap<>();

                                profileMap.put("name",setUsername);

                                mdatabase.child("Users").child(currUserId).updateChildren(profileMap).
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    sendToMainActivity();
                                                    Toast.makeText(registerActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    String msg = task.getException().toString();
                                                    Toast.makeText(registerActivity.this, "Error" +msg, Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });

//                                sendToMainActivity();

                                Toast.makeText(registerActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                            } else {
                                mdialogue.dismiss();
                                String msg = task.getException().toString();
                                Toast.makeText(registerActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendToMainActivity() {
        Intent intent = new Intent(registerActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void InitializeFeilds() {
        edtEmail = findViewById(R.id.regisEmail);
        edtPass = findViewById(R.id.regisPass);
        edtName = findViewById(R.id.regisName);

        btnCreate = findViewById(R.id.btnCreateAcc);
        mdialogue = new ProgressDialog(this);
        textHaveAcc = findViewById(R.id.textHaveAcc);


    }

}