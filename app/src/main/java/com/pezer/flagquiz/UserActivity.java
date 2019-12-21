package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private String mTitle = "Scoreboard";

    //  UI Element used to display the user's scoreboard
    private ListView mScores;
    private List<Map<String, String>> mScoreboard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    //  Menu ID constants
    private final int GLOBALSCORES_MENU_ID = Menu.FIRST;
    private final int LOGOUT_MENU_ID = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();

        mScores = findViewById(R.id.userScoreListView);
        mScoreboard = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            Log.i(TAG, mAuth.getCurrentUser().getEmail());
            final FirebaseUser user = mAuth.getCurrentUser();

            //  Displays the user's e-mail address in the ActionBar
            setTitle(user.getEmail());

            //  Obtains the scoreboard for this user from Firestore
            mFirestore = FirebaseFirestore.getInstance();
            DocumentReference document = mFirestore.collection("scoreboards").document(user.getEmail());
            document.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();

                        if (snapshot.exists()) {
                            Log.i(TAG, snapshot.getData().toString());
                            Log.i(TAG, "LOOK!!! " + snapshot.getData().get("result").toString());
                            //  GET THE LIST FROM THE SNAPSHOT AND PASS IT TO LISTVIEW ADAPTER ETC
                            mScoreboard = ((List<Map<String, String>>) snapshot.getData().get("result"));
                            for (int i = 0; i < mScoreboard.size(); i++) Log.i(TAG, "READING SCORES: " + mScoreboard.get(i).toString());

                            ArrayAdapter<Map<String, String>> arrayAdapter = new ArrayAdapter<Map<String, String>>(UserActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    mScoreboard);
                            mScores.setAdapter(arrayAdapter);

                        }

                        else Log.i(TAG, "No data for " + user.getEmail());
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, GLOBALSCORES_MENU_ID, Menu.NONE, "View Global Scoreboard");
        menu.add(Menu.NONE, LOGOUT_MENU_ID, Menu.NONE, "Log Out");

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
