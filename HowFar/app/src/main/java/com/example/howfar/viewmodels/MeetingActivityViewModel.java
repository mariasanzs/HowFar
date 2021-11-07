package com.example.howfar.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.howfar.model.MqttContent;
import com.example.howfar.model.Participant;
import com.example.howfar.paho.PahoClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MeetingActivityViewModel extends AndroidViewModel implements LocationListener {
    private MutableLiveData<Participant> participants;
    private MutableLiveData<Location> currentLocation;
    private MutableLiveData<LatLng> meetingPointLocation;
    private ArrayList<String> topics = new ArrayList<>();
    private Application application;
    private PahoClient pahoClient;
    private LocationManager locationManager;
    final Handler handler = new Handler();


    public MeetingActivityViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    }

    public MutableLiveData<Participant> getParticipants() {
        if (participants == null) {
            participants = new MutableLiveData<>();
        }
        return participants;
    }

    public MutableLiveData<Location> getCurrentLocation() {
        if (currentLocation == null) {
            currentLocation = new MutableLiveData<>();
        }
        return currentLocation;
    }

    public MutableLiveData<LatLng> getMeetingPointLocation() {
        if (meetingPointLocation == null) {
            meetingPointLocation = new MutableLiveData<>();
        }
        return meetingPointLocation;
    }

    public void beginRequestingLocation() {
        if (ContextCompat.checkSelfPermission(application.getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 10, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        }
    }

    public void initalizeMqttClient(String meetingId) {
        topics.add(meetingId.toString() + "/location");
        topics.add(meetingId.toString() + "/distance");
        pahoClient = new PahoClient(application, topics);
        if (!pahoClient.getLastReceivedMessage().hasObservers()) {
            pahoClient.getLastReceivedMessage().observeForever(msg -> onMessageArrived(msg));
        }
    }

    public void publishMeetingPointLocation(Double lat, Double longit) {
        LatLng meetpoint = new LatLng(lat,longit);
        meetingPointLocation = getMeetingPointLocation();
        meetingPointLocation.postValue(meetpoint);
        pahoClient.publishMessage(topics.get(0),lat+":"+longit);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = getCurrentLocation();
        currentLocation.postValue(location);
        // Calcular distancia al sitio
        Location meetingpoint = new Location("");
        LatLng latlongmeeting = meetingPointLocation.getValue();
        meetingpoint.setLatitude(latlongmeeting.latitude);
        meetingpoint.setLongitude(latlongmeeting.longitude);
        float fdistance = location.distanceTo(meetingpoint);
        Integer distance = new Integer((int) fdistance);
        // Publicar distancia
        String nickname = pahoClient.getUserNickname();
        String messagecontent = nickname+":"+distance.toString();
        pahoClient.publishMessage(topics.get(1),messagecontent);

    }

    private void onMessageArrived(MqttContent message) {
        participants = getParticipants();
        meetingPointLocation = getMeetingPointLocation();
        if (message.topic.contains("distance")) {
            String[] pieces = message.payload.split(":");
            if (pieces.length >= 2) {
                String nickname = pieces[0];
                Integer distance = Integer.parseInt(pieces[1]);
                Participant participant = new Participant(nickname, distance);
                participants.postValue(participant);
            }
        } else if (message.topic.contains("location")) {
            String[] pieces = message.payload.split(":");
            if (pieces.length >= 2) {
                Double latitude = Double.valueOf(pieces[0]);
                Double longitude = Double.valueOf(pieces[1]);
                LatLng latLng = new LatLng(latitude, longitude);
                meetingPointLocation.postValue(latLng);
            }
        }
    }
}
