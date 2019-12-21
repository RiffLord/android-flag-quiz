package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

//  Prompts the user to sign up or log in with an e-mail address & password using Firebase Authentication
public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";

    private EditText mEmailEditText;
    private EditText mPassEditText;

    private FirebaseAuth mAuth; //  Handles user account creation & login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = findViewById(R.id.emailEditText);
        mPassEditText = findViewById(R.id.passwordEditText);

        //  Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //  A single button is used to handle account creation and signing in existing users
        final Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals(""))
                    createAccount(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                else {
                    Toast.makeText(LoginActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //  Checks if user is already signed in, upating UI accordingly
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.i(TAG, "Current user: " + user.getEmail());
            //  Call startQuiz
            mEmailEditText.setText(user.getEmail());
        }
    }

    //
    private void startQuiz() {
        Intent loginIntent = new Intent(this, QuizActivity.class);
        startActivity(loginIntent);
    }

    //  Uses FirebaseAuth to create a new user account with e-mail & password
    private void createAccount(final String email, final String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {   //  New user created
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    signIn(email, pass);
                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    //   If the credentials correspond to an existing user, log them in
                    signIn(email, pass);
                } else { //  Invalid e-mail address or password or any other error
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                    //   Clear the edit text boxes
                    mEmailEditText.setText("");
                    mPassEditText.setText("");
                }
            }
        });
    }

    //  Signs in a user to Firebase
    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    startQuiz();    //  Begins the quiz
                } else {
                    Log.w(TAG, "signInWithEmail:failure");
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
