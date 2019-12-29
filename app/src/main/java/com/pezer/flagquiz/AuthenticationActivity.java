package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

//  Prompts the user to sign up or log in with an e-mail address & password using Firebase Authentication.
public class AuthenticationActivity extends AppCompatActivity {

    //  UI Elements
    private TextInputEditText mEmailEditText;
    private TextInputEditText mPassEditText;
    private MaterialButton mLoginButton;
    private MaterialButton mRegisterButton;

    private LinearLayout mAuthProgressLayout;   //  Secondary layout for authentication.
    private boolean authProgress = false;       //  Used to check the state of the Activity's layout.

    //  Handles user account creation & login.
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        setTitle(R.string.login);

        //  Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        //  Setting up GUI components.
        mEmailEditText = findViewById(R.id.emailEditText);
        mPassEditText = findViewById(R.id.passwordEditText);

        mAuthProgressLayout = findViewById(R.id.authLayout);

        mRegisterButton = findViewById(R.id.registerButton);
        mLoginButton = findViewById(R.id.loginButton);

        //  Setting up listeners for the buttons.
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Disables the layout.
                hideKeyboard(AuthenticationActivity.this);
                callAuthProgressLayout();
                mRegisterButton.setEnabled(false);
                mLoginButton.setEnabled(false);

                //  Ensures that the e-mail and password EditTexts aren't empty.
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals("")) {
                    createAccount(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                } else {
                    Toast.makeText(AuthenticationActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                    restoreLayout();    //  Allows the user to try entering their credentials again.
                }
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Disables the layout while the authentication process lasts.
                callAuthProgressLayout();
                mLoginButton.setEnabled(false);
                mRegisterButton.setEnabled(false);

                //  Ensures that the e-mail and password EditTexts aren't empty.
                if (!mEmailEditText.getText().toString().equals("") && !mPassEditText.getText().toString().equals("")) {
                    signIn(mEmailEditText.getText().toString(), mPassEditText.getText().toString());
                } else {
                    Toast.makeText(AuthenticationActivity.this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show();
                    restoreLayout();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //  Checks the flag and restores the layout if necessary, enabling control.
        if (authProgress) restoreLayout();

        //  Checks if user is already signed in, updating UI accordingly.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
            mEmailEditText.setText(user.getEmail());
    }

    //  Takes the user to the main Activity.
    private void startQuiz() {
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified()) {
                Intent quizIntent = new Intent(this, QuizActivity.class);
                startActivity(quizIntent);
            } else {
                //  If the user hasn't verified their account, prompts them to do so.
                verifyAccount();
            }
        }
    }

    //============================== Authentication Methods ==============================//

    //  Uses FirebaseAuth to create a new user account with the e-mail & password provided.
    private void createAccount(final String email, final String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    displayError(task.getException());

                    //  Restores layout, allowing the user to try again with new credentials.
                    clearEditTexts();
                    restoreLayout();
                } else {
                    //  User account successfully created.
                    setDisplayName(mAuth.getCurrentUser(), email, pass);
                }
            }
        });
    }

    //  Checks if the user's account is verified
    private void verifyAccount() {
        //  The prompt to verify the account is presented in an AlertDialog
        AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(this);

        verifyDialogBuilder.setTitle(R.string.verify_account_dialog);
        verifyDialogBuilder.setMessage("Please check your e-mail for the verification link to activate your account.");

        verifyDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //  Checks if the user is verified (from another device for example)
                                if (mAuth.getCurrentUser().isEmailVerified()) {
                                    startQuiz();
                                } else {

                                    //  If the user isn't verified, opens a chooser for the
                                    //  device's e-mail clients to allow the user to complete
                                    //  the process.
                                    Toast.makeText(AuthenticationActivity.this,
                                            "Please verify your account before proceeding",
                                            Toast.LENGTH_SHORT).show();

                                    Intent emailClientIntent = new Intent(Intent.ACTION_MAIN);
                                    emailClientIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                    try {
                                        startActivity(emailClientIntent);
                                    } catch (ActivityNotFoundException e) {
                                        displayError(e);
                                    }
                                }
                            } else displayError(task.getException());
                        }
                    });
            }
        });

        //  Option to resend verification link, in case the user hasn't received it yet.
        verifyDialogBuilder.setNeutralButton("Resend Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.getCurrentUser().sendEmailVerification();

                restoreLayout();
            }
        });

        AlertDialog verifyDialog = verifyDialogBuilder.create();

        verifyDialog.show();
    }

    //  Prompts the user to choose their display name.
    private void setDisplayName(final FirebaseUser firebaseUser, final String email, final String pass) {
        //  Creates a Dialog for the prompt.
        final Dialog displayNameDialog = new Dialog(AuthenticationActivity.this);
        displayNameDialog.setContentView(R.layout.dialog_display_name);

        MaterialButton setDisplayNameButton = displayNameDialog.findViewById(R.id.setDisplayNameButton);
        setDisplayNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Obtains a reference to the EditText and assigns its contents to the displayName String.
                TextInputEditText displayNameEditText = displayNameDialog.findViewById(R.id.displayNameEditText);
                final String displayName = displayNameEditText.getText().toString();

                if (!displayName.equals("")) {  //  The EditText is not empty...
                    displayNameDialog.dismiss();

                    //  Sends a verification e-mail to the address provided
                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                //  Updates the user's profile with the display name entered in the EditText
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
                    //  Notifies the user that the EditText is empty.
                    Toast.makeText(AuthenticationActivity.this,
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
                    displayError(task.getException());

                    //  Restores layout, allowing the user to try again with new credentials.
                    clearEditTexts();
                    restoreLayout();
                } else
                    startQuiz();    //  User has been successfully authenticated.
            }
        });
    }

    //============================== Utility Methods ==============================//

    //  Converts the provided Exception to a string and displays it in a Toast.
    private void displayError(Exception e) {
        String error = e.toString();

        //  Formats the error message and displays it.
        Toast.makeText(AuthenticationActivity.this, error.substring(error.lastIndexOf(": "))
                .replace(": ", ""), Toast.LENGTH_LONG).show();
    }

    //  Clears the EditTexts if an error occurs during authentication.
    private void clearEditTexts() {
        //   Clear the edit text boxes
        mEmailEditText.getText().clear();
        mPassEditText.getText().clear();
    }

    //  Dims the screen and displays a ProgressBar during authentication.
    private void callAuthProgressLayout() {
        authProgress = true;    //  User is being authenticated.

        hideKeyboard(AuthenticationActivity.this);

        //  Disables all UI elements.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //  Dims the screen.
        mAuthProgressLayout.setVisibility(View.VISIBLE);
    }

    //  Hides the keyboard.
    private static void hideKeyboard(Activity activity) {
        InputMethodManager input = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);

        //  Obtains the currently focused view, allowing the correct window token to be accessed from it
        View view = activity.getCurrentFocus();

        if (view == null) view = new View(activity);

        input.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //  Restores the original layout and makes the UI clickable
    private void restoreLayout() {
        mAuthProgressLayout.setVisibility(View.GONE);

        //  Restores control.
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mLoginButton.setEnabled(true);
        mRegisterButton.setEnabled(true);

        authProgress = false;   //  User is no longer being authenticated.
    }
}
