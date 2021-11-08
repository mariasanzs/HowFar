package com.example.howfar.viewmodels;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
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

import java.nio.channels.MulticastChannel;
import java.util.ArrayList;

public class MeetingActivityViewModel extends AndroidViewModel implements
        LocationListener,
        PahoClientListener
{
    private MutableLiveData<Participant> participants;
    private MutableLiveData<Location> currentLocation;
    private MutableLiveData<LatLng> meetingPointLocation;
    private MutableLiveData<PahoClient.ConnectionStatus> pahoClientConnectionStatus;
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

    public MutableLiveData<PahoClient.ConnectionStatus> getPahoClientConnectionStatus() {
        if (pahoClientConnectionStatus == null) {
            pahoClientConnectionStatus = new MutableLiveData<>();
        }
        return pahoClientConnectionStatus;
    }

    public MutableLiveData<LatLng> getMeetingPointLocation() {
        if (meetingPointLocation == null) {
            meetingPointLocation = new MutableLiveData<>();
            Log.d("PAHOJOIN","El meeting point location estaba a null");

        }
        return meetingPointLocation;
    }

    public void beginRequestingLocation() {
        if (ContextCompat.checkSelfPermission(application.getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        }
    }

    public void initalizeMqttClient(String meetingId) {
        topics.add(meetingId + "/location");
        topics.add(meetingId + "/distance");
        pahoClient = new PahoClient(application, topics, this);
        if (!pahoClient.getLastReceivedMessage().hasObservers()) {
            pahoClient.getLastReceivedMessage().observeForever(msg -> onMessageArrived(msg));
        }
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
        getCurrentLocation().postValue(location);
        // Calcular distancia al sitio
        Location meetingPoint = new Location("");
        LatLng latLongMeeting = getMeetingPointLocation().getValue();
        if (latLongMeeting != null && pahoClient.isConnected()) {
            meetingPoint.setLatitude(latLongMeeting.latitude);
            meetingPoint.setLongitude(latLongMeeting.longitude);
            float fdistance = location.distanceTo(meetingPoint);
            Integer distance = new Integer((int) fdistance);
            // Publicar distancia
            String nickname = pahoClient.getUserNickname();
            String messageContent = nickname + ":" + distance.toString();
            pahoClient.publishMessage(topics.get(1), messageContent, false);
        }
    }

    private void onMessageArrived(MqttContent message) {
        if (message.topic.contains("distance")) {
            String[] pieces = message.payload.split(":");
            if (pieces.length >= 2) {
                String nickname = pieces[0];
                Integer distance = Integer.parseInt(pieces[1]);
                Participant participant = new Participant(nickname, distance);
                getParticipants().postValue(participant);
            }
        } else if (message.topic.contains("location")) {
            String[] pieces = message.payload.split(":");
            if (pieces.length >= 2) {
                Double latitude = Double.valueOf(pieces[0]);
                Double longitude = Double.valueOf(pieces[1]);
                LatLng latLng = new LatLng(latitude, longitude);
                getMeetingPointLocation().postValue(latLng);
                Log.d("PAHOJOIN","Se recibe mensaje de meeting point location");
            }
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
}
