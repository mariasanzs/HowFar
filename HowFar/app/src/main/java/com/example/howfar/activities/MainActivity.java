package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.howfar.R;
import com.example.howfar.model.Place;
import com.example.howfar.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnKeyListener {
    private Button createMeetButton;
    private Button joinMeetButton;
    private Button goJoinFormButton;
    private Button cancelJoinFormButton;
    private EditText nameField;
    private EditText joinId;
    private TextView helloText;
    private View tintView;
    private ConstraintLayout joinMeetForm;
    private MainActivityViewModel viewModel;
    private List<Place> places = new ArrayList<>();
    private String nickname;
    private String joinIdMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        initializeViews();
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
        joinId= findViewById(R.id.nameField);
        joinId.addTextChangedListener(this);
        joinId.setOnKeyListener(this);


    }

    private void showJoinForm() {
        tintView.setVisibility(View.VISIBLE);
        tintView.animate().alpha(0.5f);
        joinMeetForm.setVisibility(View.VISIBLE);
        joinMeetForm.animate().alpha(1.0f);
        nameField.setEnabled(false);
        createMeetButton.setEnabled(false);
        joinMeetButton.setEnabled(false);
        joinIdMeeting = joinId.getText().toString();

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
        Intent i = new Intent(this, MeetingActivity.class);
        i.putExtra("idMeeting",joinIdMeeting);
        startActivity(i);
    }

    private boolean validateNameField() {
        if (nameField.getText().toString().matches("^[a-zA-Z0-9]{4,}$")) {
            nickname = nameField.getText().toString();
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
            Intent intent = new Intent(this  , CreateMeetActivity.class);
            intent.putExtra("nickname",nickname);
            startActivity(intent);

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