package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private String mTitle = "Scoreboard";
    //  TODO:   set up ListView and Adapter to display user's scores

    //  TODO: logout option and highscoresactivity option in menu

    //  UI Elements
    private TextView mEmail;
    private Button mLogoutButton;
    private ListView mScores;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.i(TAG, "in onCreate...");

        mAuth = FirebaseAuth.getInstance();

        mScores = findViewById(R.id.userScoreListView);

        if (mAuth.getCurrentUser() != null) {
            Log.i(TAG, mAuth.getCurrentUser().getEmail());
            FirebaseUser user = mAuth.getCurrentUser();

            //  TODO: check if this works
            setTitle(user.getEmail());

            //  TODO: adapt to FlagQuiz database structure
            // Get ${LIMIT} restaurants
            /*mQuery = mFirestore.collection("QUIZ-SCOREBOARD").document(mEmail.getText().toString)
                    .orderBy("RESULT-%-SUBSTRING", Query.Direction.DESCENDING)
                    .limit(LIMIT);*/
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
