package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private List<Place> places = new ArrayList<>();
    private boolean listofcinemasinitialized = false;
    public static final String LOGSLOADWEBCONTENT = "LOGSLOADWEBCONTENT";

    private static final String URL_CINEMAS = "https://datos.madrid.es/egob/catalogo/208862-7650046-ocio_salas.json";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        es = Executors.newSingleThreadExecutor();
        initializeViews();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                // message received from background thread: load complete (or failure)
                String string_result;
                super.handleMessage(msg);
                if ((string_result = msg.getData().getString("text")) != null) {
                    if (listofcinemasinitialized == false) {
                        Log.d(LOGSLOADWEBCONTENT, "message received from background thread");
                        try {
                            Log.d(LOGSLOADWEBCONTENT, string_result);
                            JSONObject obj = new JSONObject(string_result);
                            // fetch JSONObject named employee
                            JSONArray graph = obj.getJSONArray("@graph");
                            for (int i = 0; i < graph.length(); i++) {
                                // create a JSONObject for fetching single user data
                                JSONObject cinema = graph.getJSONObject(i);
                                String title = cinema.getString("title");
                                JSONObject location = cinema.getJSONObject("location");
                                double latitude = location.getDouble("latitude");
                                double longitude = location.getDouble("longitude");
                                Log.d(LOGSLOADWEBCONTENT, String.valueOf(longitude));
                                places.add(new Place(title,longitude,latitude));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listofcinemasinitialized = true;
                        showListCinemas();
                    }
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
        Bundle args = new Bundle();
        intent.putExtra("places", (Serializable) places);
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