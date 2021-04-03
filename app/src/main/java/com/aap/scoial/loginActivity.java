package com.aap.scoial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class loginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    Button btnLogin,btnPhone;
    EditText edtEmail, edtPass;
    TextView textForgotpass, textCreateAcc;
    ProgressDialog dialogue;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("UsersChat");
//        currentUser = mAuth.getCurrentUser();

        InitializeFeilds();
        textCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(loginActivity.this,registerActivity.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSign();
            }
        });

        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(loginActivity.this,phoneLoginActivity.class));
            }
        });
    }

    private void userSign() {
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

        dialogue.setTitle("Signing in...");
        dialogue.setMessage("Just a moment, Logging you in Ritz7Chat");
        dialogue.setCanceledOnTouchOutside(true);
        dialogue.show();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String currentUserId = mAuth.getCurrentUser().getUid();
                String deviceToken = FirebaseInstanceId.getInstance().getToken();

//                UsersRef.child(currentUserId).child("device_token")
//                        .setValue(deviceToken)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful())
//                                {
//                                    sendToMainActivity();
//                                    Toast.makeText(loginActivity.this, "Logged in Successful...", Toast.LENGTH_SHORT).show();
//                                    dialogue.dismiss();
//                                }
//                            }
//                        });
                if (task.isSuccessful()){
                    Toast.makeText(loginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                    dialogue.dismiss();
                    sendToMainActivity();
                    Toast.makeText(loginActivity.this, "Welcome To Social", Toast.LENGTH_SHORT).show();
                }else{
                    dialogue.dismiss();
                    String err = task.getException().toString();
                    Toast.makeText(getApplicationContext(),err,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void InitializeFeilds() {
        edtEmail = findViewById(R.id.textEmaillog);
        edtPass = findViewById(R.id.textPasslog);
        btnLogin = findViewById(R.id.btnLoginact);
        btnPhone = findViewById(R.id.btnPhone);
        textForgotpass = findViewById(R.id.textForgot);
        textCreateAcc = findViewById(R.id.textCreateAcc);
        dialogue = new ProgressDialog(this);

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(currentUser !=  null){
//            sendToMainActivity();
//        }
//    }

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(loginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}