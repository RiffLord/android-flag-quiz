package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//  Prompts the user to sign up or log in with an e-mail address & password using Firebase
public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //  Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        final Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //  Checks if user is already signed in, upating UI accordingly
        //FirebaseUser user = mAuth.getCurrentUser();
        //Log.i(TAG, "Current user: " + user.getEmail().toString());
    }

    private void login() {
        Intent loginIntent = new Intent(this, QuizActivity.class);
        startActivity(loginIntent);
    }
}
