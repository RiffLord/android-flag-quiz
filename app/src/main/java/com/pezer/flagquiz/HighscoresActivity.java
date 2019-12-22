package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighscoresActivity extends AppCompatActivity {
    private static final String TAG = "HighscoresActivity";

    FirebaseFirestore mFirestore;


    //  TODO: if user's own score is in the scores, onClick takes them to their personal scoreboard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        mFirestore = FirebaseFirestore.getInstance();

        //  Obtains the scoreboard for this user from Firestore
        mFirestore = FirebaseFirestore.getInstance();
        CollectionReference collection = mFirestore.collection("scoreboards");
        Query query = collection;
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId());
                        Query highscore = document.getReference().collection("quiz-results");
                        highscore.limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> mScoreboard = new ArrayList<>();

                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        Log.i(TAG, snapshot.getData().get("score").toString());
                                        String highscore = snapshot.getReference().getParent().getParent().getId() + ": " + snapshot.getData().get("score").toString();
                                        mScoreboard.add(highscore);
                                    }

                                    ListView mHighscores = findViewById(R.id.highscoreListView);
                                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(HighscoresActivity.this, android.R.layout.simple_list_item_1, mScoreboard);
                                    mHighscores.setAdapter(mAdapter);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
