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
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private String mTitle = "Scoreboard";

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();

            //  Displays the user's e-mail address on the ActionBar
            setTitle(user.getEmail());

            //  Obtains this user's scoreboard from Firestore
            mFirestore = FirebaseFirestore.getInstance();
            Query query = mFirestore.collection("scoreboards").document(user.getEmail()).collection("quiz-results");
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<String> scoreboard = new ArrayList<>();

                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            Log.i(TAG, snapshot.getData().get("score").toString());
                            scoreboard.add(snapshot.getData().get("score").toString());
                        }

                        ListView scoreList = findViewById(R.id.userScoreListView);
                        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(UserActivity.this, android.R.layout.simple_list_item_1, scoreboard);
                        scoreList.setAdapter(listAdapter);
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
