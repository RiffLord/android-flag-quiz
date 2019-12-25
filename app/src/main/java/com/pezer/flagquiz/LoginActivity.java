package com.pezer.flagquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

//  Prompts the user to sign up or log in with an e-mail address & password using Firebase Authentication
public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";

    //  UI Elements
    private EditText mEmailEditText;
    private EditText mPassEditText;

    private LinearLayout mAuthLayout;
    private boolean authProgress = false;

    //  Handles user account creation & login
    private FirebaseAuth mAuth;
    private String mError;

    //  TODO: code cleanup & refactoring
    //  TODO: add Material Design elements

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = findViewById(R.id.emailEditText);
        mPassEditText = findViewById(R.id.passwordEditText);

        mAuthLayout = findViewById(R.id.authLayout);

        //  Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //  A single button is used to handle account creation and signing in existing users
        final Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivity.this);
                //  Disables the layout while the authentication process lasts
                callAuthLayout();
                loginButton.setEnabled(false);
                //  Ensures that the e-mail and password EditTexts aren't empty
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals("")) {
                    //  TODO: add check to ensure the entered e-mail address exists
                    createAccount(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (authProgress = true) restoreLayout();

        //  Checks if user is already signed in, upating UI accordingly
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mEmailEditText.setText(user.getEmail());
        }
    }

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
                    //  TODO: implement a display name for use in HighscoresActivity, check for valid e-mail address
                    signIn(email, pass);
                } else { //  Invalid e-mail address or password or any other error
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        //   If the credentials correspond to an existing user, log them in
                        signIn(email, pass);
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        //  If invalid e-mail address is given
                        displayError(mError, task.getException());
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        //  If wrong password is given
                        displayError(mError, task.getException());
                    } else {
                        displayError(mError, task.getException());
                    }

                    //   Clear the edit text boxes
                    mEmailEditText.setText("");
                    mPassEditText.setText("");
                }
            }
        });
    }

    //  Signs in an existing user to Firebase
    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                //  TODO: check for correct password
                if (task.isSuccessful()) {
                    startQuiz();    //  Begins the quiz
                } else {
                    Log.e(TAG, "signInWithEmail:failure... " + task.getException().toString());
                    displayError(mError, task.getException());

                    restoreLayout();
                }
            }
        });
    }

    private void displayError(String error, Exception e) {
        error = e.toString();

        //  Formats the error message and displays it in a Toast.
        Toast.makeText(LoginActivity.this, error.substring(error.lastIndexOf(": "))
                .replace(": ", ""), Toast.LENGTH_SHORT).show();
    }


    private void callAuthLayout() {
        authProgress = true;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mAuthLayout.setVisibility(View.VISIBLE);
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager input = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);

        //  Obtains the currently focused view, allowing the correct window token to be accessed from it
        View view = activity.getCurrentFocus();

        if (view == null) view = new View(activity);

        input.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void restoreLayout() {
        //  Restores the original layout and makes the UI clickable
        mAuthLayout.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setEnabled(true);

        authProgress = false;
    }
}
