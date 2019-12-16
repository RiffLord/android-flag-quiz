package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class UserActivity extends AppCompatActivity {
    private static String TAG = "UserActivity";
    //  TODO:   set up ListView and Adapter to display user's scores

    //  UI Elements
    private TextView mEmail;
    private Button mLogoutButton;
    private ListView mScores;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();

        //mUsername.setText(username);
        mEmail = findViewById(R.id.emailTextView);
        //mEmail.setText(email);
        mScores = findViewById(R.id.userScoreListView);

        if (mAuth.getCurrentUser() != null) {
            mEmail.setText(mAuth.getCurrentUser().getEmail());
        }

        mLogoutButton = findViewById(R.id.logoutButton);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }
}
