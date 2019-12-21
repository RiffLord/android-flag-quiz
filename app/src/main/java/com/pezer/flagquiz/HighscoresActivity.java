package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HighscoresActivity extends AppCompatActivity {
    private static final String TAG = "HighscoresActivity";

    FirebaseFirestore mFirestore;

    //  UI
    ListView mHighscores;
    List<Map<String, String>> mScoreboard;


    //  TODO: if user's own score is in the scores, onClick takes them to their personal scoreboard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        mFirestore = FirebaseFirestore.getInstance();
        //QUERY=foreach DOCUMENT get SCORE order DESCENDING LIMIT 1
        //String mUser = foreach DOCUMENT.toString()
        //LISTADAPTERSOMETHING = user + score

        mScoreboard = new ArrayList<>();

        //  Obtains the scoreboard for this user from Firestore
        mFirestore = FirebaseFirestore.getInstance();
    }
}
