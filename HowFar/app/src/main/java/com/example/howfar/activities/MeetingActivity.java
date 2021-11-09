package com.example.howfar.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.adapter.HistoryAdapter;
import com.example.howfar.fragments.MapsFragment;
import com.example.howfar.model.Participant;
import com.example.howfar.paho.PahoClient;
import com.example.howfar.viewmodels.MeetingActivityViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.UUID;

public class MeetingActivity  extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean creator = false;
    private Double lat;
    private Double longit;
    private MapsFragment mapFragment;
    private String meetId;
    private MeetingActivityViewModel viewModel;
    private ProgressDialog progressDialog;
    HistoryAdapter mAdapter;
    RecyclerView mRecyclerView;
    FloatingActionButton fab;
    private Button finishButton;


    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.mipmap.ic_launcher);
        progressDialog.setMessage("Creating meet...");
        progressDialog.show();
        setContentView(R.layout.meeting_layout);
        fab = findViewById(R.id.fab);
        finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(view -> finishButtonPressed());

        mRecyclerView = findViewById(R.id.history_recycler_view);
        viewModel = new ViewModelProvider(this).get(MeetingActivityViewModel.class);

        requestLocationPermissions();
        intent = getIntent();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HistoryAdapter(new HashMap<>());
        mRecyclerView.setAdapter(mAdapter);

        setupMapFragment();

        if (intent.getBooleanExtra("meetingCreator", false)) {
            creator = true;
            meetId = UUID.randomUUID().toString();
            meetId = meetId.substring(0,7);
        } else {
            creator = false;
            meetId = intent.getStringExtra("idMeeting");
        }
        viewModel.initalizeMqttClient(meetId);
        viewModel.setNickname(getIntent().getStringExtra("nickname"));
        viewModel.getParticipants()
                .observe(this, participant -> onParticipantDistanceChanged(participant));
        viewModel.getCurrentLocation()
                .observe(this, location -> onLocationChanged(location));
        viewModel.getPahoClientConnectionStatus()
                .observe(this, status -> onPahoClientConnectionStatusChanged(status));
        fab.setOnClickListener(view -> sharingId());
    }

    private void finishButtonPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sharingId() {
        //FUNCIONA
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Click to share meetingID", meetId);
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
        if (lat != 0 && longit != 0) { // Not creator
            mapFragment.setMarker(new LatLng(lat, longit));
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            viewModel.beginRequestingLocation();
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
            viewModel.beginRequestingLocation();
        } else {
            requestLocationPermissions();
        }
    }

   private void onLocationChanged(Location location) {
        Toast.makeText(this, location.getLatitude() + ":" + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    private void onParticipantDistanceChanged(Participant participant) {
        // Añadir participant a la lista y refrescarla
        String nickname = participant.nickname;
        String distance = Integer.toString(participant.distanceToLocation);
        mAdapter.add(nickname, distance + "m");
        mAdapter.notifyDataSetChanged();
    }

    private void onMeetingPointLocationReceived(LatLng location) {
        // Meter un toast
        Log.d("PAHOJOIN","Meeting location recieved");
        Toast.makeText(this,"Meeting location recieved",Toast.LENGTH_SHORT);
        mapFragment.setMarker(location);
        viewModel.getCurrentLocation().removeObservers(this);
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

    private void onPahoClientConnectionStatusChanged(PahoClient.ConnectionStatus status) {
        if (status.equals(PahoClient.ConnectionStatus.SUCCEEDED)) {
            if (creator) {
                lat = intent.getDoubleExtra("placeLatitude", 0);
                longit = intent.getDoubleExtra("placeLongitude", 0);
                viewModel.publishMeetingPointLocation(lat, longit);
            } else if (!viewModel.getMeetingPointLocation().hasObservers()) {
                Log.d("PAHOJOIN", "ENTRA Aquí");
                viewModel.getMeetingPointLocation()
                        .observe(this, latLng -> onMeetingPointLocationReceived(latLng));
            }
            progressDialog.dismiss();
        } else {
            Toast.makeText(this, "An error ocurred", Toast.LENGTH_LONG).show();
        }
    }

}
