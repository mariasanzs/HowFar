package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.howfar.R;
import com.example.howfar.viewmodels.MainActivityViewModel;

import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnKeyListener {
    private Button createMeetButton;
    private Button joinMeetButton;
    private Button goJoinFormButton;
    private Button cancelJoinFormButton;
    private EditText nameField;
    private TextView helloText;
    private View tintView;
    private ConstraintLayout joinMeetForm;
    private MainActivityViewModel viewModel;
    Handler handler;
    ExecutorService es;

    private static final String URL_CINEMAS = "https://datos.madrid.es/portal/site/egob/menuitem.ac61933d6ee3c31cae77ae7784f1a5a0/?vgnextoid=00149033f2201410VgnVCM100000171f5a0aRCRD&format=json&file=7650046&filename=208862-7650046-ocio_salas&mgmtid=842385ce457a8410VgnVCM2000000c205a0aRCRD&preview=full";
    private static final String CONTENT_TYPE_JSON = "application/json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        initializeViews();
        // Define the handler that will receive the information from the background thread:
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                // message received from background thread: load complete (or failure)
                String string_result;
                super.handleMessage(msg);
                if ((string_result = msg.getData().getString("text")) != null) {
                    

                }
            }
        };

    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.activityStopped();
    }

    private void initializeViews() {
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

    private void showListCinemas(){
        Intent intent = new Intent(this  , ListPlacesActivity.class);
        startActivity(intent);
    }

    private void hideJoinForm() {
        joinMeetForm.setVisibility(View.GONE);
        tintView.setVisibility(View.GONE);
        joinMeetForm.setAlpha(0.0f);
        tintView.setAlpha(0.0f);
        nameField.setEnabled(true);
        createMeetButton.setEnabled(true);
        joinMeetButton.setEnabled(true);
    }

    private void goJoinFormButtonPressed() {

    }

    private boolean validateNameField() {
        if (nameField.getText().toString().matches("^[a-zA-Z0-9]{4,}$")) {
            return true;
        }
        nameField.setError("Nickname must contain at least 4 non special characters");
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
        if (validateNameField()){
            LoadWebContent loadURLContentsjson = new LoadWebContent(handler,CONTENT_TYPE_JSON, URL_CINEMAS);
            es.execute(loadURLContentsjson);
            showListCinemas();
        }
    }

    private void joinMeetButtonPressed() {
        if (validateNameField()) {
            showJoinForm();
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