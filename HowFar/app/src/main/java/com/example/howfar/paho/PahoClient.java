package com.example.howfar.paho;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.howfar.R;
import com.example.howfar.model.MqttContent;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class PahoClient {
    public enum ConnectionStatus { SUCCEEDED, FAILURE };
    private PahoClientListener listener;
    final String serverUri = "tcp://broker.hivemq.com:1883";
    private final String preferencesFile;
    String lastWillMessage = "Goodbye!";
    MqttAndroidClient mqttAndroidClient;
    String clientId;
    String userNickname;
    private SharedPreferences preferences;
    private MutableLiveData<MqttContent> lastReceivedMessage;
    private final String NICKNAMEKEY = "userName";
    ArrayList<String> clientTopics;
    public PahoClient(Application application, ArrayList<String> topics, PahoClientListener listener) {
        this.listener = listener;
        preferencesFile = application.getString(R.string.shared_preferences_file);
        preferences = application.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        userNickname = preferences.getString(NICKNAMEKEY, "");
        clientId = clientId + System.currentTimeMillis();
        clientTopics = topics;
        mqttAndroidClient = new MqttAndroidClient(application.getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                    for (String topic : clientTopics) {
                        subscribeToTopic(topic);
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                // Not implemented
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                MqttContent mqttContent = new MqttContent(topic, payload);
                lastReceivedMessage.postValue(mqttContent);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        byte[] payload = lastWillMessage.getBytes();
        mqttConnectOptions.setWill("distance",payload,0,false);
        mqttConnectOptions.setCleanSession(true);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    for (String topic : clientTopics) {
                        subscribeToTopic(topic);
                    }
                    listener.onConnectionCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PAHOJOIN", "Failed to connect to: " + serverUri +
                            ". Cause: " + ((exception.getCause() == null)?
                            exception.toString() : exception.getCause()));
                    listener.onConnectionFailed();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("PAHOJOIN","Subscribed to " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PAHOJOIN","Failed to subsribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void log(String mainText) {
        System.out.println("LOG: " + mainText);
    }

    public void publishMessage(String topic, String publishMessage) {
        MqttMessage message = new MqttMessage();
        message.setPayload(publishMessage.getBytes());
        Log.d("PAHOJOIN","config mensaje");
        message.setRetained(true);
        message.setQos(1);
        try {
            mqttAndroidClient.publish(topic,message);
            Log.d("PAHOJOIN","mensaje enviado");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PAHOJOIN",e.toString());
        }
    }

    public MutableLiveData<MqttContent> getLastReceivedMessage() {
        if (lastReceivedMessage == null) {
            lastReceivedMessage = new MutableLiveData<>();
        }
        return lastReceivedMessage;
    }

    public String getUserNickname(){
        return userNickname;
    }

    public boolean isConnected() {
        return mqttAndroidClient.isConnected();
    }
}
