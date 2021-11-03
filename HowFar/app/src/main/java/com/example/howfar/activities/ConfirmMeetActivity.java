package com.example.howfar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.R;
import com.example.howfar.fragments.MapsFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class ConfirmMeetActivity extends AppCompatActivity {
    private Intent intent;
    private MapsFragment mapFragment;
    private TextView textView;
    private RadioGroup radioGroup;
    private ImageButton directionsButton;
    private SharedPreferences preferences;
    private int mapType;
    private String preferencesFile = "preferencesFile";
    private final String mapTypeKey = "mapType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        intent = getIntent();

        preferences = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        mapType = preferences.getInt(mapTypeKey, GoogleMap.MAP_TYPE_NORMAL);

        openGMaps();
        setupMapFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void openGMaps() {
        Double lat = intent.getDoubleExtra("latitude", 0);
        Double longit = intent.getDoubleExtra("longitude", 0);
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + longit);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void setupMapFragment() {
        mapFragment = new MapsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainerView2, mapFragment)
                .commit();
        Double lat = intent.getDoubleExtra("latitude", 0);
        Double longit = intent.getDoubleExtra("longitude", 0);
        mapFragment.setMarker(new LatLng(lat, longit));
        mapFragment.setType(mapType);
    }


}
