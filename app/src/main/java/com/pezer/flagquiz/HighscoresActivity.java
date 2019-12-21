package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

public class HighscoresActivity extends AppCompatActivity {
    FirebaseFirestore mFirestore;

    //  TODO: if user's own score is in the scores, onClick takes them to their personal scoreboard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        mFirestore = FirebaseFirestore.getInstance();
        //QUERY=foreach DOCUMENT get SCORE order DESCENDING LIMIT 1
        //String mUser = foreach DOCUMENT.toString()
        //LISTADAPTERSOMETHING = user + score
    }
}
