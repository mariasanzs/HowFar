package com.example.howfar.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.adapter.HistoryAdapter;
import com.example.howfar.fragments.MapsFragment;
import com.example.howfar.paho.PahoClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.UUID;

public class MeetingActivity  extends AppCompatActivity
        implements LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private PahoClient client;
    private String idMeeting ="02991";
    private Handler handler = new Handler();
    private final String topic0 ="distance";
    private final String topic1 ="location";
    private boolean creator = false;
    private Double lat;
    private Double longit;
    private MapsFragment mapFragment;
    private LocationManager locationManager;
    private UUID meetId;
    HistoryAdapter mAdapter;
    RecyclerView mRecyclerView;
    FloatingActionButton fab;
    ArrayList<String> topics = new ArrayList<>();
    Intent intent;
    Runnable runnable;
    int delay = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_layout);
        fab = findViewById(R.id.fab);
        mRecyclerView = findViewById(R.id.history_recycler_view);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestLocationPermissions();
        initListOfTopics();
        intent = getIntent();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HistoryAdapter(new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);

        setupMapFragment();

        if (intent.getBooleanExtra("meetingCreator", false)) {
            creator = true;
            meetId = UUID.randomUUID();

            //idMeeting = ...
        } else {
            creator = false;
            //Coger idmeeting de la actividad de join
            //idMeeting=...

        }
        fab.setOnClickListener(view -> sharingId());
        client = new PahoClient(getApplication(), mAdapter, topics);
        //client.subscribeToTopic();

    }

    private void initListOfTopics() {
        topics.add(topic0);
        topics.add(topic1);
    }

    private void sharingId() {
        //FUNCIONA
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Click to share meetingID", idMeeting);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MeetingActivity.this, "Meeting ID copied to clipboard!",
                Toast.LENGTH_SHORT).show();
    }

    private void setupMapFragment() {
        mapFragment = new MapsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.meetingMapFragment, mapFragment)
                .commit();
        lat = intent.getDoubleExtra("placeLatitude", 0);
        longit = intent.getDoubleExtra("placeLongitude", 0);
        mapFragment.setMarker(new LatLng(lat, longit));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(MeetingActivity.this,
                "Current location is " + location.getLongitude() + ":" + location.getLatitude(),
                Toast.LENGTH_SHORT)
            .show();
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableLocation();
        } else {
            requestLocationPermissions();
        }

    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        } else {
            requestLocationPermissions();
        }
    }

    private boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

}
