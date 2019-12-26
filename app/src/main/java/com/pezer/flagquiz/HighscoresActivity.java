package com.pezer.flagquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HighscoresActivity extends AppCompatActivity {
    private static final String TAG = "HighscoresActivity";

    private final int QUIZ_MENU_ID = Menu.FIRST;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        setTitle(R.string.global_scoreboard);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //  Obtains the global scoreboard from Firestore
        mFirestore = FirebaseFirestore.getInstance();
        Query query = mFirestore.collection("scoreboards");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    final List<String> highscores = new ArrayList<>();

                    //  Iterates through the user documents
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        final String user = document.getData().get("user").toString();

                        //  Obtains the high score for each user
                        final Query highscore = document.getReference().collection("quiz-results");
                        highscore.limit(1).orderBy("score", Query.Direction.ASCENDING).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                        //  Stores the username and high score and adds it o the list
                                        String highscoreString = user + ": " + snapshot.getData().get("score").toString();

                                        highscores.add(highscoreString);
                                    }

                                    //  TODO: order highscores

                                    //  Displays the high scores on screen
                                    ListView scoreboard = findViewById(R.id.highscoreListView);
                                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(HighscoresActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            highscores);
                                    scoreboard.setAdapter(mAdapter);

                                    //  If user clicks on their high score, takes them to their personal scoreboard Activity
                                    scoreboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            String username = (String) parent.getItemAtPosition(position);

                                            if (username.substring(0, username.lastIndexOf(':'))
                                                    .equals(mAuth.getCurrentUser().getDisplayName())) {
                                                Intent userScoreboardIntent = new Intent(HighscoresActivity.this,
                                                        UserActivity.class);
                                                startActivity(userScoreboardIntent);
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, task.getException().getMessage());
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, QUIZ_MENU_ID, Menu.NONE, R.string.quiz);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //  Takes the user back to the quiz.
        if (item.getItemId() == QUIZ_MENU_ID)
            startActivity(new Intent(this, QuizActivity.class));

        return super.onOptionsItemSelected(item);
    }
}
