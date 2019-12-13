package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {
    private static String TAG = "UserActivity";
    //  TODO:   set up ListView and Adapter to display user's scores
    //  TODO:   configure this activity to communicate with Firebase

    //  UI Elements
    private TextView mUsername;
    private TextView mEmail;
    private ListView mScores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUsername = findViewById(R.id.usernameTextView);
        //mUsername.setText(username);
        mEmail = findViewById(R.id.emailTextView);
        //mEmail.setText(email);
        mScores = findViewById(R.id.userScoreListView);
    }
}
