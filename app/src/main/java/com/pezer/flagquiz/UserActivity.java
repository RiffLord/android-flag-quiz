package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    //  Keys for Firestore access
    private static final String USER_COLLECTION = "scoreboards";
    private static final String SCORE_COLLECTION = "quiz-results";
    private static final String SCORE_KEY = "score";

    //  Menu ID constants
    private final int GLOBALSCORES_MENU_ID = Menu.FIRST;
    private final int LOGOUT_MENU_ID = Menu.FIRST + 1;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //  Checks if user exists/is logged in
        if (mAuth.getCurrentUser() != null) {
            final FirebaseUser user = mAuth.getCurrentUser();

            //  Displays the user's e-mail address on the ActionBar
            setTitle(user.getEmail());

            //  Obtains this user's scoreboard from Firestore
            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            Query query = mFirestore.collection(USER_COLLECTION).document(user.getEmail()).collection(SCORE_COLLECTION);
            query.orderBy(SCORE_KEY, Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<String> scoreboard = new ArrayList<>();    //  List for storing scores

                        //  Iterates through the documents containing the user's scores, adding them to the list
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            scoreboard.add(snapshot.getData().get(SCORE_KEY).toString());
                        }

                        //  Displays the user's scoreboard on screen
                        ListView scoreList = findViewById(R.id.userScoreListView);
                        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                                UserActivity.this,
                                android.R.layout.simple_list_item_1,
                                scoreboard);
                        scoreList.setAdapter(listAdapter);
                    } else {
                        Log.e(TAG, task.getException().toString());

                        Toast.makeText(UserActivity.this, "Error reading scoreboard for " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, GLOBALSCORES_MENU_ID, Menu.NONE, R.string.highscores).setIcon(R.drawable.baseline_emoji_events_black_18dp_2);
        menu.add(Menu.NONE, LOGOUT_MENU_ID, Menu.NONE, R.string.logout).setIcon(R.drawable.baseline_account_circle_black_18dp_2);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case GLOBALSCORES_MENU_ID:
                Intent highscoresIntent = new Intent(this, HighscoresActivity.class);
                startActivity(highscoresIntent);
                break;
            case LOGOUT_MENU_ID:
                signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }
}
