package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.howfar.R;
import com.example.howfar.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnKeyListener{
    private Button createMeetButton;
    private Button joinMeetButton;
    private Button goJoinFormButton;
    private Button cancelJoinFormButton;
    private EditText nameField;
    private TextView helloText;
    private View tintView;
    private ConstraintLayout joinMeetForm;
    private MainActivityViewModel viewModel;
    private List<Place> places = new ArrayList<>();
    private boolean listofcinemasinitialized = false;
    private SwitchCompat bAccess;
    private TextToSpeech mTTS;
    //STT
    TextView micTextTitle;
    ImageButton micVoiceButton;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Configuration of Text To Speech (volume and speed)
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not suported");
                    }else{
                        //if everything is succesful
                        mTTS.setPitch(0.5f);
                        mTTS.setSpeechRate(0.5f);
                    }
                }else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        //STT
        micTextTitle = findViewById(R.id.micTitle);
        micVoiceButton = findViewById(R.id.voiceButton);
        // button click to show speech to text dialog
        micVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();
            }
        });
        //
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (!viewModel.getSensorStatus().hasObservers()) {
            viewModel.getSensorStatus().observe(this, bool -> onProximitySensorChanged(bool));
        }
        initializeViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.activityStopped();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (viewModel.appShouldTalk) {
            mTTS.speak("Introduce your nickname", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void onProximitySensorChanged(Boolean bool) {
        if (bool) {
            bAccess.setChecked(true);
            mTTS.speak("Introduce your nickname", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void initializeViews() {
        bAccess = findViewById(R.id.bAccess);
        bAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                viewModel.switchChanged(b);
                if (b) {
                    mTTS.speak("Introduce your nickname", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
        bAccess.setChecked(viewModel.appShouldTalk);
        tintView = findViewById(R.id.tintView);
        tintView.setVisibility(View.GONE);
        tintView.setAlpha(0.0f);
        joinMeetForm = findViewById(R.id.joinFormView);
        joinMeetForm.setVisibility(View.GONE);
        joinMeetForm.setAlpha(1.0f);
        goJoinFormButton = findViewById(R.id.goJoinFormButton);
        goJoinFormButton.setOnClickListener(view -> goJoinFormButtonPressed());
        cancelJoinFormButton = findViewById(R.id.cancelJoinFormButton);
        cancelJoinFormButton.setOnClickListener(view -> hideJoinForm());
        createMeetButton = findViewById(R.id.createMeetButton);
        createMeetButton.setOnClickListener(view -> createMeetButtonPressed());
        joinMeetButton = findViewById(R.id.joinMeetButton);
        joinMeetButton.setOnClickListener(view -> joinMeetButtonPressed());
        nameField = findViewById(R.id.nameField);
        nameField.addTextChangedListener(this);
        nameField.setOnKeyListener(this);
        helloText = findViewById(R.id.helloTextView);
        setHelloText(viewModel.getNickname());
        if (viewModel.getNickname().trim() != "") {
            nameField.setText(viewModel.getNickname());
        }
    }

    private void showJoinForm() {
        tintView.setVisibility(View.VISIBLE);
        tintView.animate().alpha(0.5f);
        joinMeetForm.setVisibility(View.VISIBLE);
        joinMeetForm.animate().alpha(1.0f);
        nameField.setEnabled(false);
        createMeetButton.setEnabled(false);
        joinMeetButton.setEnabled(false);
    }


    private void hideJoinForm() {
        joinMeetForm.setVisibility(View.GONE);
        tintView.setVisibility(View.GONE);
        joinMeetForm.setAlpha(0.0f);
        tintView.setAlpha(0.0f);
        nameField.setEnabled(true);
        createMeetButton.setEnabled(true);
        joinMeetButton.setEnabled(true);
        if (viewModel.appShouldTalk) {
            mTTS.speak("Cancelling going to the meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void goJoinFormButtonPressed() {
        if (viewModel.appShouldTalk) {
            mTTS.speak("Going to the Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private boolean validateNameField() {
        if (nameField.getText().toString().matches("^[a-zA-Z0-9]{4,}$")) {
            return true;
        }
        nameField.setError("Nickname must contain at least 4 non special characters");
        if (viewModel.appShouldTalk) {
            mTTS.speak("Invalid Nickname", TextToSpeech.QUEUE_FLUSH, null);
        }
        return false;
    }

    private void setHelloText(String nickname) {
        if (nickname.trim().equals("")) {
            helloText.setText("Hello!");
        } else {
            helloText.setText("Hello " + nickname.trim() + "!");
        }
    }

    private void createMeetButtonPressed() {
        if (viewModel.appShouldTalk) {
            mTTS.speak("Creating Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
        if (validateNameField()){
            Intent intent = new Intent(this  , CreateMeetActivity.class);
            startActivity(intent);
        }
    }

    private void joinMeetButtonPressed() {
        if (viewModel.appShouldTalk) {
            mTTS.speak("Join Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
        if (validateNameField()) {
            showJoinForm();
            if (viewModel.appShouldTalk) {
                mTTS.speak("Enter ID Meeting", TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        return;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String name = nameField.getText().toString();
        setHelloText(name);
        viewModel.nicknameChanged(name);
        if (viewModel.appShouldTalk) {
            mTTS.speak(nameField.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        return;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nameField.getWindowToken(), 0);
            nameField.clearFocus();
            joinMeetButton.clearFocus();
            createMeetButton.clearFocus();
        }
        return false;
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){
            Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT:{
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                micTextTitle.setText(result.get(0));
            }
            break;
        }
    }
}

