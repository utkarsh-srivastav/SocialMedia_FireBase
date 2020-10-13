package com.example.socialmediafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtEmail, edtPassword, edtPhone, edtVCode, edtUsername;
    private Button btnSignUp, btnLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtVCode = findViewById(R.id.edtVCode);
        edtUsername = findViewById(R.id.edtUsername);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        findViewById(R.id.btnSendVCode).setOnClickListener(this);
        findViewById(R.id.btnPhoneSignUp).setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);


        mAuth = FirebaseAuth.getInstance();




    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            transitionToSocialMediaActivity();
        }
    }


    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnSignUp:
                    signup();
                    break;

                case R.id.btnLogin:
                    signin();
                    break;

                case R.id.btnSendVCode:
                    Toast.makeText(MainActivity.this, "Not implemented",
                            Toast.LENGTH_SHORT).show();
                    break;

                case R.id.btnPhoneSignUp:
                    Toast.makeText(MainActivity.this, "Not implemented yet",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transitionToSocialMediaActivity() {
        Intent intent = new Intent(this, SocialMediaActivity.class);
        startActivity(intent);
    }



    private void signup(){
        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(MainActivity.this, "SignUp Successful",
                            Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference()
                            .child("my_users").child(task.getResult()
                            .getUser().getUid()).child("username")
                            .setValue(edtUsername.getText().toString());

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(edtUsername.getText().toString())
                            .build();

                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "Display Name Updated",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    transitionToSocialMediaActivity();

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void signin(){
        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Login Successful",
                                    Toast.LENGTH_SHORT).show();
                            transitionToSocialMediaActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}