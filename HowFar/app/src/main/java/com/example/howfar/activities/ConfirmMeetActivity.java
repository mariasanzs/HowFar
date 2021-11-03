package com.example.howfar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.R;
import com.example.howfar.fragments.MapsFragment;
import com.google.android.gms.maps.model.LatLng;

public class ConfirmMeetActivity extends AppCompatActivity {
    private Intent intent;
    private MapsFragment mapFragment;
    private TextView placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        placeName = findViewById(R.id.placeName);
        intent = getIntent();
        placeName.setText(intent.getStringExtra("placeName"));
        setupMapFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupMapFragment() {
        mapFragment = new MapsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainerView, mapFragment)
                .commit();
        Double lat = intent.getDoubleExtra("placeLatitude", 0);
        Double longit = intent.getDoubleExtra("placeLongitude", 0);
        mapFragment.setMarker(new LatLng(lat, longit));
    }
}
