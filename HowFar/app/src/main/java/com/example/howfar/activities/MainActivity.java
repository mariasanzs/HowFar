package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnKeyListener, SensorEventListener {
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
    private SwitchCompat bProx;
    private SensorManager sensorManager;
    private Sensor ProxSensor;
    private TextToSpeech mTTS;
    private boolean proxSensorActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Proximity Sensor to activate Text To Speech
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ProxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
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
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        initializeViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getSensorStatus().observe(this, bool -> publishSensorStatus(bool));
        viewModel.switchChanged(proxSensorActive);
        viewModel.activityStopped();
    }
    @Override
    protected void onStart() {
        super.onStart();
        viewModel.getSensorStatus().observe(this, bool -> publishSensorStatus(bool));
        bProx.setChecked(proxSensorActive);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(this, ProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this, ProxSensor);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        Toast.makeText(this, "status: main", Toast.LENGTH_SHORT).show();
        viewModel.onSensorChanged(sensorEvent);
        viewModel.getSensorStatus().observe(this, bool -> publishSensorStatus(bool));
        bProx.setChecked(proxSensorActive);
        mTTS.speak("Introduce your nickname", TextToSpeech.QUEUE_FLUSH, null);
        //proxSensorActive = viewModel.getSensorStatus();
        if(proxSensorActive){
            Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "far", Toast.LENGTH_SHORT).show();
        }
    }

    public void publishSensorStatus(Boolean bool){
        Toast.makeText(this, "status:" + bool, Toast.LENGTH_SHORT).show();
        proxSensorActive =  bool;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void initializeViews() {
        bProx = findViewById(R.id.bProx);
        bProx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                viewModel.switchChanged(b);
            }
        });
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
        if (proxSensorActive) {
            mTTS.speak("Cancelling going to the meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void goJoinFormButtonPressed() {
        if (proxSensorActive) {
            mTTS.speak("Going to the Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private boolean validateNameField() {
        if (nameField.getText().toString().matches("^[a-zA-Z0-9]{4,}$")) {
            return true;
        }
        nameField.setError("Nickname must contain at least 4 non special characters");
        if (proxSensorActive) {
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
        if (proxSensorActive) {
            mTTS.speak("Creating Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
        if (validateNameField()){
            Intent intent = new Intent(this  , CreateMeetActivity.class);
            startActivity(intent);
        }
    }

    private void joinMeetButtonPressed() {
        if (proxSensorActive) {
            mTTS.speak("Join Meeting", TextToSpeech.QUEUE_FLUSH, null);
        }
        if (validateNameField()) {
            showJoinForm();
            if (proxSensorActive) {
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
        if (proxSensorActive) {
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
}