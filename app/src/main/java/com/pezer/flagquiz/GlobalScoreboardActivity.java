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
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlobalScoreboardActivity extends AppCompatActivity {
    private static final String TAG = "GlobalScoreboard";

    //  Menu ID constant
    private final int QUIZ_MENU_ID = Menu.FIRST;

    //  Firebase access
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_scoreboard);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        setTitle(R.string.global_scoreboard);

        //  Obtains the global scoreboard from Firestore
        mFirestore = FirebaseFirestore.getInstance();

        Query query = mFirestore.collection("scoreboards");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    //  Used to store the high score for each user
                    final List<String> highScoresList = new ArrayList<>();

                    //  Iterates through the user documents
                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        //  Stores the user's display name
                        final String user = userDocument.getData().get("user").toString();

                        //  Obtains the high score for each user
                        final Query highScore = userDocument.getReference().collection("quiz-results");
                        highScore.limit(1).orderBy("score", Query.Direction.ASCENDING).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot highScoreDocument : task.getResult()) {

                                        //  The user's display name and high score are stored in a String and added to the list
                                        String highScoreString = highScoreDocument.getData().get("score").toString() + ": " + user;
                                        highScoresList.add(highScoreString);
                                    }

                                    //  Sorts the list of high scores
                                    Collections.sort(highScoresList);

                                    //  Displays the high scores on screen
                                    ListView scoreboard = findViewById(R.id.highscoreListView);
                                    ArrayAdapter<String> mAdapter = new ArrayAdapter<>(GlobalScoreboardActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            highScoresList);
                                    scoreboard.setAdapter(mAdapter);

                                    //  If user clicks on their high score, takes them to their personal scoreboard Activity
                                    scoreboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            String username = (String) parent.getItemAtPosition(position);

                                            if (username.contains(mAuth.getCurrentUser().getDisplayName())) {
                                                Intent userScoreboardIntent = new Intent(GlobalScoreboardActivity.this,
                                                        UserInfoActivity.class);
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
        menu.add(Menu.NONE, QUIZ_MENU_ID, Menu.NONE, R.string.quiz).setShowAsAction(1);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //  Takes the user back to the quiz.
        if (item.getItemId() == QUIZ_MENU_ID) {
            startActivity(new Intent(this, QuizActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
