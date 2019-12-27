package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
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
import com.google.firebase.auth.UserProfileChangeRequest;

//  Prompts the user to sign up or log in with an e-mail address & password using Firebase Authentication
public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";

    //  UI Elements
    private EditText mEmailEditText;
    private EditText mPassEditText;

    private Button mLoginButton;
    private Button mRegisterButton;

    private LinearLayout mAuthLayout;
    private boolean authProgress = false;

    //  Handles user account creation & login
    private FirebaseAuth mAuth;

    //  TODO: code cleanup & refactoring

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        mEmailEditText = findViewById(R.id.emailEditText);
        mPassEditText = findViewById(R.id.passwordEditText);

        mAuthLayout = findViewById(R.id.authLayout);

        //  Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        mRegisterButton = findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivity.this);
                callAuthProgressLayout();
                mRegisterButton.setEnabled(false);
                mLoginButton.setEnabled(false);

                //  Ensures that the e-mail and password EditTexts aren't empty
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals("")) {
                    createAccount(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                    restoreLayout();
                }
            }
        });

        mLoginButton = findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivity.this);
                //  Disables the layout while the authentication process lasts
                callAuthProgressLayout();
                mLoginButton.setEnabled(false);
                mRegisterButton.setEnabled(false);

                //  Ensures that the e-mail and password EditTexts aren't empty
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals("")) {
                    signIn(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                    restoreLayout();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (authProgress) restoreLayout();

        //  Checks if user is already signed in, updating UI accordingly
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
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Log.e(TAG, "Invalid credentials..." + e.getMessage());
                        displayError(e);
                    } catch (FirebaseAuthUserCollisionException e) {
                        Log.e(TAG, "User with this e-mail address already exists..." + e.getMessage());
                        displayError(e);
                    } catch (Exception e) {
                        displayError(e);
                    }

                    clearEditTexts();
                    restoreLayout();
                } else {
                    setDisplayName(mAuth.getCurrentUser(), email, pass);
                }
            }
        });
    }

    //  Allows user to choose their display name
    private void setDisplayName(final FirebaseUser firebaseUser, final String email, final String pass) {
        final Dialog displayNameDialog = new Dialog(LoginActivity.this);
        displayNameDialog.setContentView(R.layout.dialog_display_name);

        Button setDisplayNameButton = displayNameDialog.findViewById(R.id.setDisplayNameButton);
        setDisplayNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = displayNameDialog.findViewById(R.id.displayNameEditText);
                final String displayName = editText.getText().toString();

                if (!displayName.equals("")) {
                    displayNameDialog.dismiss();

                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(displayName).build();
                                firebaseUser.updateProfile(profileUpdates);

                                signIn(email, pass);
                            } else {
                                displayError(task.getException());
                            }
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Please choose a display name",
                            Toast.LENGTH_LONG).show();
                }

            }
        });

        displayNameDialog.show();
    }

    //  Signs in an existing user to Firebase
    private void signIn(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        Log.e(TAG, "This e-mail doesn't exist..." + e.getMessage());
                        displayError(e);
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Log.e(TAG, "Wrong password..." + e.getMessage());
                        displayError(e);
                    } catch (Exception e) {
                        displayError(e);
                    }

                    clearEditTexts();
                    restoreLayout();
                } else startQuiz();
            }
        });
    }

    private void displayError(Exception e) {
        String error = e.toString();

        //  Formats the error message and displays it in a Toast.
        Toast.makeText(LoginActivity.this, error.substring(error.lastIndexOf(": "))
                .replace(": ", ""), Toast.LENGTH_LONG).show();
    }

    private void clearEditTexts() {
        //   Clear the edit text boxes
        mEmailEditText.getText().clear();
        mPassEditText.getText().clear();
    }

    private void callAuthProgressLayout() {
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

    //  Restores the original layout and makes the UI clickable
    private void restoreLayout() {
        mAuthLayout.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mLoginButton.setEnabled(true);
        mRegisterButton.setEnabled(true);

        authProgress = false;
    }
}
