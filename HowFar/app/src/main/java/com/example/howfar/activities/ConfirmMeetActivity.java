package com.example.howfar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.R;
import com.example.howfar.fragments.MapsFragment;
import com.google.android.gms.maps.model.LatLng;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
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
