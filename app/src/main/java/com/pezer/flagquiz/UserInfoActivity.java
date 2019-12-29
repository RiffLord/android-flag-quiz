package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.content.Intent;
import android.os.Bundle;
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

//  Allows the user to view data relevant to them, including a personal scoreboard and the e-mail address
//  used for the account. Allows the user to log out of the game.
public class UserInfoActivity extends AppCompatActivity {

    //  Keys for Firestore access
    private static final String USER_COLLECTION = "scoreboards";
    private static final String SCORE_COLLECTION = "quiz-results";
    private static final String SCORE_KEY = "score";

    private FirebaseAuth mAuth;

    //  Menu ID constants
    private final int GLOBAL_SCOREBOARD_MENU_ID = Menu.FIRST;
    private final int LOGOUT_MENU_ID = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

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

            getPersonalScoreboard(user);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, GLOBAL_SCOREBOARD_MENU_ID, Menu.NONE, R.string.high_scores).setIcon(R.drawable.baseline_emoji_events_black_18dp_2);
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
            case GLOBAL_SCOREBOARD_MENU_ID:
                //  Takes the user to the global scoreboard Activity.
                Intent globalScoreboardIntent = new Intent(this, GlobalScoreboardActivity.class);
                startActivity(globalScoreboardIntent);
                break;
            case LOGOUT_MENU_ID:
                signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPersonalScoreboard(final FirebaseUser u) {
        //  Obtains this user's scoreboard from Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        //  Firestore query, asks for the user's scores in ascending order (lower number of guesses = higher score).
        Query query = firestore.collection(USER_COLLECTION).document(u.getEmail()).collection(SCORE_COLLECTION);
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
                            UserInfoActivity.this,
                            android.R.layout.simple_list_item_1,
                            scoreboard);
                    scoreList.setAdapter(listAdapter);
                } else {
                    Toast.makeText(UserInfoActivity.this, "Error reading scoreboard for " + u.getDisplayName(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        if (mAuth.getCurrentUser() == null) {
            //  Takes the user back to the Login screen.
            Intent loginIntent = new Intent(this, AuthenticationActivity.class);
            startActivity(loginIntent);
        }
    }
}
