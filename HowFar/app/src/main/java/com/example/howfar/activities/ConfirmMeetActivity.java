package com.example.howfar.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.R;
import com.example.howfar.fragments.MapsFragment;
import com.google.android.gms.maps.model.LatLng;

public class ConfirmMeetActivity extends AppCompatActivity {
    private Intent intent;
    private MapsFragment mapFragment;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        intent = getIntent();
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
    }


}
