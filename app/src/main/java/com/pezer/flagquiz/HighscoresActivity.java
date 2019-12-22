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
import com.google.firebase.firestore.QuerySnapshot;

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

        mScoreboard = new ArrayList<>();

        //  Obtains the scoreboard for this user from Firestore
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        CollectionReference collection = mFirestore.collection("scoreboards");
        collection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot snapshot : task.getResult()) {
                        Log.i(TAG, snapshot.getId());
                        snapshot.getReference().collection("quiz-results").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot resultSnapshot : task.getResult()) {
                                        Log.i(TAG, resultSnapshot.getData().get("score").toString());
                                    }
                                }
                            }
                        });
                    }
                }

            }
        });
    }
}
