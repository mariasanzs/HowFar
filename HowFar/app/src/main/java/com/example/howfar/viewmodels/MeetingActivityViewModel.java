package com.example.howfar.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.howfar.model.MqttContent;
import com.example.howfar.model.Participant;
import com.example.howfar.paho.PahoClient;
import com.example.howfar.paho.PahoClientListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class MeetingActivityViewModel extends AndroidViewModel implements
        LocationListener,
        PahoClientListener
{
    private MutableLiveData<ArrayList<Participant>> participants;
    private HashMap<String, Participant> participantsMap;
    private MutableLiveData<LatLng> meetingPointLocation;
    private MutableLiveData<PahoClient.ConnectionStatus> pahoClientConnectionStatus;
    private ArrayList<String> topics = new ArrayList<>();
    private Application application;
    private PahoClient pahoClient;
    private LocationManager locationManager;
    private String nickname;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public MeetingActivityViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        participantsMap = new HashMap<>();
        locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
    }

    public MutableLiveData<ArrayList<Participant>> getParticipants() {
        if (participants == null) {
            participants = new MutableLiveData<>();
        }
        return participants;
    }

    public MutableLiveData<PahoClient.ConnectionStatus> getPahoClientConnectionStatus() {
        if (pahoClientConnectionStatus == null) {
            pahoClientConnectionStatus = new MutableLiveData<>();
        }
        return pahoClientConnectionStatus;
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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
        }
    }

    public void initalizeMqttClient(String meetingId) {
        topics.add(meetingId + "/location");
        topics.add(meetingId + "/distance");
        pahoClient = new PahoClient(application, topics, this);
    }

    public void publishMeetingPointLocation(Double lat, Double longit) {
        LatLng meetPoint = new LatLng(lat,longit);
        meetingPointLocation = getMeetingPointLocation();
        meetingPointLocation.postValue(meetPoint);
        String messageContent = lat.toString() + ":" + longit.toString();
        pahoClient.publishMessage(topics.get(0), messageContent, true);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        publishCurrentDistanceToMeetingPointFrom(location);
    }

    private void publishCurrentDistanceToMeetingPointFrom(Location location) {
        Location meetingPoint = new Location("");
        LatLng latLongMeeting = getMeetingPointLocation().getValue();
        if (latLongMeeting != null && pahoClient.isConnected()) {
            meetingPoint.setLatitude(latLongMeeting.latitude);
            meetingPoint.setLongitude(latLongMeeting.longitude);
            float fDistance = location.distanceTo(meetingPoint);
            Integer distance = new Integer((int) fDistance);
            String nickname = this.nickname;
            String messageContent = nickname + ":" + distance.toString();
            pahoClient.publishMessage(topics.get(1), messageContent, true);
        }
    }

    private Location getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(application.getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER ) != null) {
                Log.d("HOWFARLOG","View Model: Last Known GPS Location returned");
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.d("HOWFARLOG","View Model: Last Known NETWORK Location returned");
                return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        return null;
    }

    public void onMessageArrived() {
        boolean participantsListChanged = false;
        while (!pahoClient.messagesQueue.isEmpty()) { // Process all queued messages
            MqttContent message = pahoClient.messagesQueue.remove(0);
            Log.d("HOWFARLOG", "View model has just received a message from topic" + message.topic + " " + message.payload);
            if (message.topic.contains("distance")) {
                String[] pieces = message.payload.split(":");
                if (pieces.length >= 2) {
                    String nickname = pieces[0];
                    String distance = pieces[1] + "m";
                    participantsMap.put(nickname, new Participant(nickname, distance));
                    participantsListChanged = true;
                }
            } else if (message.topic.contains("location")) {
                String[] pieces = message.payload.split(":");
                if (pieces.length >= 2) {
                    Double latitude = Double.valueOf(pieces[0]);
                    Double longitude = Double.valueOf(pieces[1]);
                    LatLng latLng = new LatLng(latitude, longitude);
                    getMeetingPointLocation().setValue(latLng);
                    Log.d("HOWFARLOG", "View model has just posted meeting point location");
                    Location lastLocation = getLastKnownLocation();
                    if (getLastKnownLocation() != null) {
                        publishCurrentDistanceToMeetingPointFrom(lastLocation);
                    }
                }
            }
        }
        if (participantsListChanged) {
            participants.postValue(new ArrayList<>(participantsMap.values()));
        }
    }

    @Override
    public void onConnectionCompleted() {
        getPahoClientConnectionStatus().postValue(PahoClient.ConnectionStatus.SUCCEEDED);
    }

    @Override
    public void onConnectionFailed() {
        getPahoClientConnectionStatus().postValue(PahoClient.ConnectionStatus.FAILURE);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        locationManager.removeUpdates(this);
        pahoClient.disconnect();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        return;
    }
}
