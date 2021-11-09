package com.example.howfar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.howfar.R;
import com.example.howfar.fragments.MapsFragment;
import com.example.howfar.viewmodels.MainActivityViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class ConfirmMeetActivity extends AppCompatActivity {
    private Intent intent;
    private Intent toMeetActivity;
    private MapsFragment mapFragment;
    private TextView placeName;
    private Button bConfirm;
    private String placeTitle;
    private Double lat;
    private Double longit;
    private String nickname;
    private TextToSpeech mTTS;
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
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
        placeName = findViewById(R.id.placeName);
        bConfirm = findViewById(R.id.buttonConfirm);
        bConfirm.setOnClickListener(view -> clickConfirmMeeting());
        intent = getIntent();
        placeTitle = intent.getStringExtra("placeName");
        placeName.setText(placeTitle);
        setupMapFragment();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void clickConfirmMeeting(){
        if (viewModel.appShouldTalk) {
            mTTS.speak("Meeting confirmed at " + placeTitle, TextToSpeech.QUEUE_FLUSH, null);
        }
        toMeetActivity = new Intent(this ,MeetingActivity.class);
        toMeetActivity.putExtra("placeLatitude", lat);
        toMeetActivity.putExtra("placeLongitude", longit);
        toMeetActivity.putExtra("placeName",placeTitle);
        toMeetActivity.putExtra("meetingCreator",true);
        startActivity(toMeetActivity);


    }

    private void setupMapFragment() {
        mapFragment = new MapsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainerView, mapFragment)
                .commit();
        lat = intent.getDoubleExtra("placeLatitude", 0);
        longit = intent.getDoubleExtra("placeLongitude", 0);
        mapFragment.setMarker(new LatLng(lat, longit));
    }
}
