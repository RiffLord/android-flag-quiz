package com.pezer.flagquiz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.AssetManager;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";

    //  Shared Preferences resources
    private static final String PREF_CHOICES = "choices_sharedPref";
    private static final String PREF_REGIONS = "regions_sharedPref";
    SharedPreferences mQuizSettings;

    //  Quiz objects & variables
    private List<String> mFilenameList;
    private List<String> mQuizCountriesList;
    private Map<String, Boolean> mRegionsMap;   //  Stores the regions used for the current quiz session
    private String m_sCorrectAnswer;
    private int m_nTotalGuesses;
    private int m_nCorrectAnswers;
    private int m_nGuessRows;

    private Random mRandom;
    private Handler mHandler;
    private Animation mShakeAnimation;

    //  UI Elements
    private TextView mAnswerTextView;
    private TextView mQuestionNumberTextView;
    private ImageView mFlagImageView;
    private TableLayout mButtonTableLayout;

    //  Menu ID constants
    private final int CHOICES_MENU_ID = Menu.FIRST;
    private final int REGIONS_MENU_ID = Menu.FIRST + 1;
    private final int USER_MENU_ID = Menu.FIRST + 2;
    private final int HIGHSCORES_MENU_ID = Menu.FIRST + 3;

    private FirebaseAuth mAuth;

    //============================== Override Methods ==============================//

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mAuth = FirebaseAuth.getInstance();

        //  Initializing everything necessary for the quiz

        mFilenameList = new ArrayList<>();
        mQuizCountriesList = new ArrayList<>();
        mRegionsMap = new HashMap<String, Boolean>();
        mRandom = new Random();
        mHandler = new Handler();

        mShakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
        mShakeAnimation.setRepeatCount(3);

        //  Obtain the array of regions from strings.xml
        String[] regionNames = getResources().getStringArray(R.array.regionsList);
        //  Choose all regions by default
        for (String region : regionNames) mRegionsMap.put(region, true);

        //  Set up GUI components
        mQuestionNumberTextView = findViewById(R.id.questionNumberTextView);
        mFlagImageView = findViewById(R.id.flagImageView);
        mButtonTableLayout = findViewById(R.id.buttonTableLayout);
        mAnswerTextView = findViewById(R.id.answerTextView);

        mQuestionNumberTextView.setText(getResources().getString(R.string.question) +
                " 1 " + getResources().getString(R.string.of) + " 10");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //  Reads the number of guess button rows from SharedPreferences, the default being 1 row.
        mQuizSettings = QuizActivity.this.getPreferences(MODE_PRIVATE);
        m_nGuessRows = mQuizSettings.getInt(PREF_CHOICES, 1);

        Gson gson = new Gson();
        String jsonRegions = mQuizSettings.getString(PREF_REGIONS, null);
        Type type = new TypeToken<HashMap<String, Boolean>>() {}.getType();
        mRegionsMap = gson.fromJson(jsonRegions, type);

        resetQuiz();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
        menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);
        menu.add(Menu.NONE, USER_MENU_ID, Menu.NONE, R.string.user);
        menu.add(Menu.NONE, HIGHSCORES_MENU_ID, Menu.NONE, R.string.highscores);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case CHOICES_MENU_ID:
                //  Creates a list of the number of answer choices
                final String[] choices = getResources().getStringArray(R.array.guessesList);

                AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
                choicesBuilder.setTitle(R.string.choices);

                //  Add the choices items to the Dialog and register an OnClickListener
                choicesBuilder.setItems(R.array.guessesList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_nGuessRows = Integer.parseInt(choices[which].toString()) / 3;

                        //  Saves the number of guesses to the SharedPreferences
                        mQuizSettings = QuizActivity.this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = mQuizSettings.edit();
                        prefEditor.putInt(PREF_CHOICES, m_nGuessRows).apply();

                        resetQuiz();
                    }
                });

                AlertDialog choicesDialog = choicesBuilder.create();
                choicesDialog.show();
                break;
            case REGIONS_MENU_ID:
                //  Creates a new array to store the regions names in; its size is equal to the size of the HashMap
                //  containing the data
                final String[] regionNames = mRegionsMap.keySet().toArray(new String[mRegionsMap.size()]);

                //  Used to determine which regions are enabled
                boolean[] enabledRegions = new boolean[mRegionsMap.size()];

                for (int i = 0; i < enabledRegions.length; ++i) enabledRegions[i] = mRegionsMap.get(regionNames[i]);

                AlertDialog.Builder regionsBuilder = new AlertDialog.Builder(this);
                regionsBuilder.setTitle(R.string.regions);

                //  Formats the strings representing the regions
                String[] formatted = new String[regionNames.length];

                for (int i = 0; i < regionNames.length; ++i) formatted[i] = regionNames[i].replace('_', ' ');

                regionsBuilder.setMultiChoiceItems(formatted, enabledRegions,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                //  Includes or excludes the clicked region depending on whether or not it's checked
                                mRegionsMap.put(regionNames[which].toString(), isChecked);
                            }
                        });


                //  Promts the user to reset the quiz with the new settings
                regionsBuilder.setPositiveButton(R.string.reset_quiz,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mQuizSettings = QuizActivity.this.getPreferences(MODE_PRIVATE);
                                SharedPreferences.Editor prefEditor = mQuizSettings.edit();
                                //  Used to transform the HashMap of included regions to a JSON object
                                Gson gson = new Gson();
                                String includedRegions = gson.toJson(mRegionsMap);
                                prefEditor.putString(PREF_REGIONS, includedRegions).apply();

                                resetQuiz();
                            }
                        });

                AlertDialog regionsDialog = regionsBuilder.create();
                regionsDialog.show();

                break;
            case USER_MENU_ID:
                Intent userIntent = new Intent(this, UserActivity.class);
                startActivity(userIntent);
                break;
            case HIGHSCORES_MENU_ID:
                Intent scoresIntent = new Intent(this, HighscoresActivity.class);
                startActivity(scoresIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mGuessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            submitGuess((Button) v);
        }
    };

    //============================== Quiz Methods ==============================//

    private void resetQuiz() {
        //  AssetManager accesses the flag images
        AssetManager assets = getAssets();
        mFilenameList.clear();  //  Clears the filename list when resetting quiz

        //  Obtains the images to all regions set to true in mRegionsMap
        //  and adds them to mFilenameList
        try {
            Set<String> regions = mRegionsMap.keySet();

            for (String region : regions) {
                if (mRegionsMap.get(region)) {
                    //  Obtain all flag image files from the corresponding region
                    String[] paths = assets.list(region);

                    for (String path : paths) mFilenameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading file names", e);
        }

        //  Resets the correct answer and total guess counters
        m_nCorrectAnswers = 0;
        m_nTotalGuesses = 0;
        mQuizCountriesList.clear(); //  Clears previous list of countries in the quiz

        int flagCounter = 1;
        int numberOfFlags = mFilenameList.size();

        while (flagCounter <= 10) {
            int randomIndex = mRandom.nextInt(numberOfFlags);

            //  Obtains a random filename and adds it to the quiz list if not already present
            String fileName = mFilenameList.get(randomIndex);
            if (!mQuizCountriesList.contains(fileName)) {
                mQuizCountriesList.add(fileName);
                ++flagCounter;
            }
        }

        loadNextFlag();
    }

    private void loadNextFlag() {
        //  Obtains the file name of the next flag and removes it from the list
        String nextImageName = mQuizCountriesList.remove(0);
        m_sCorrectAnswer = nextImageName;

        mAnswerTextView.setText("");

        //  Updates the number of the current question
        mQuestionNumberTextView.setText(getResources().getString(R.string.question) + " " +
                (m_nCorrectAnswers + 1) + " " + getResources().getString(R.string.of) + " 10");

        //  Extracts the region string from the image's name
        String region = nextImageName.substring(0, nextImageName.indexOf('-'));

        //  Loads the next image from the assets folder
        AssetManager assetManager = getAssets();
        InputStream stream;

        try {
            stream = assetManager.open(region + "/" + nextImageName + ".png");

            //  Loads the file as a Drawable and displays it in the appropriate ImageView
            Drawable flag = Drawable.createFromStream(stream, nextImageName);
            mFlagImageView.setImageDrawable(flag);
        } catch (IOException e) {
            Log.e(TAG, "Error loading " + nextImageName, e);
        }

        //  Clears previously displayed answer buttons
        for (int row = 0; row < mButtonTableLayout.getChildCount(); ++row)
            ((TableRow) mButtonTableLayout.getChildAt(row)).removeAllViews();

        Collections.shuffle(mFilenameList);

        int correct = mFilenameList.indexOf(m_sCorrectAnswer);
        mFilenameList.add(mFilenameList.remove(correct));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //  Adds the appropriate number of answer buttons based on the value of m_nGuessRows
        for (int row = 0; row < m_nGuessRows; row++) {
            TableRow currentRow = getTableRow(row);

            for (int column = 0; column < 3; column++) {
                //  Creates a new button
                Button newGuessButton = (Button) inflater.inflate(R.layout.guess_button, currentRow, false);
                newGuessButton.getLayoutParams().height = TableRow.LayoutParams.MATCH_PARENT;

                //  Obtains a country name and sets it as the button's text
                String filename = mFilenameList.get((row * 3) + column);
                newGuessButton.setText(getCountryName(filename));

                newGuessButton.setOnClickListener(mGuessButtonListener);
                currentRow.addView(newGuessButton);
            }
        }

        //  Replaces a random button with the correct answer
        int row = mRandom.nextInt(m_nGuessRows);
        int column = mRandom.nextInt(3);
        TableRow randomRow = getTableRow(row);
        String countryName = getCountryName(m_sCorrectAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    private TableRow getTableRow(int row) { return (TableRow) mButtonTableLayout.getChildAt(row); }

    //  Formats and returns the country name read from the file
    private String getCountryName(String name) { return name.substring(name.indexOf('-') + 1).replace('_', ' '); }

    private void submitGuess(Button guessButton) {
        //  Obtains the text from the button pressed by the user
        String guess = guessButton.getText().toString();
        //  Obtains the correct answer
        String answer = getCountryName(m_sCorrectAnswer);
        ++m_nTotalGuesses;  //  Increments total guesses

        if (guess.equals(answer)) {
            ++m_nCorrectAnswers;    //  Increments correct answers

            //  Displays the appropriate message on screen
            mAnswerTextView.setText(answer + "!");
            mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer));

            disableButtons();   //  Disables the answer buttons

            //  If all 10 flags have been guessed, display the results in an AlertDialog
            //  and prompt the user to reset the quiz
            if (m_nCorrectAnswers == 10) {
                AlertDialog.Builder quizResults = new AlertDialog.Builder(this);
                quizResults.setTitle(R.string.reset_quiz);

                //  Stores the result of the quiz in a string. Locale.getDefault() is optional
                String result = String.format(Locale.getDefault(), "%d %s, %.02f%% %s",
                        m_nTotalGuesses, getResources().getString(R.string.guesses),
                        (1000 / (double) m_nTotalGuesses), getResources().getString(R.string.correct));

                //  Display the results of this game
                quizResults.setMessage(result);
                quizResults.setCancelable(false);

                quizResults.setPositiveButton(R.string.reset_quiz,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetQuiz();
                            }
                        });

                AlertDialog resetDialog = quizResults.create();
                resetDialog.show();
            } else {
                //  Correct answer, but number of correct answers < number of questions
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 1000);
            }
        } else {
            //  Wrong answer
            mFlagImageView.startAnimation(mShakeAnimation); //  Plays the wrong answer animation

            //  Displays the appropriate message
            mAnswerTextView.setText(R.string.incorrect);
            mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            guessButton.setEnabled(false);  //  Disables the button
        }
    }

    private void disableButtons() {
        for (int row = 0; row < mButtonTableLayout.getChildCount(); ++row) {
            TableRow tableRow = (TableRow) mButtonTableLayout.getChildAt(row);

            //  Disables the buttons at the corresponding row
            for (int i = 0; i < tableRow.getChildCount(); ++i) tableRow.getChildAt(i).setEnabled(false);
        }
    }
}
