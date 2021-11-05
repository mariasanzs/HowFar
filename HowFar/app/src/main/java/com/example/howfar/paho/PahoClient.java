package com.example.howfar.paho;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.howfar.R;
import com.example.howfar.adapter.HistoryAdapter;
import com.google.android.material.snackbar.Snackbar;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PahoClient {
    final String serverUri = "tcp://broker.hivemq.com:1883"; //Cambiar URL
    private final String preferencesFile;
    String lastWillMessage = "Goodbye!";
    MqttAndroidClient mqttAndroidClient;
    String clientId;
    String userNickname;
    ArrayList<String> topics;
    private SharedPreferences preferences;
    private MutableLiveData<Float> lastReceivedDistance;
    private HistoryAdapter mAdapter;
    private final String NICKNAMEKEY = "userName";

    public PahoClient(Application application) {
        preferencesFile = application.getString(R.string.shared_preferences_file);
        preferences = application.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        userNickname = preferences.getString(NICKNAMEKEY, "");
        clientId = clientId + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(application.getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                // Not implemented
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String price = new String(message.getPayload());
                Float priceFloat = new Float(price);
                Log.d("PAHO", priceFloat.toString());
                lastReceivedDistance.postValue(priceFloat);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        byte[] payload = lastWillMessage.getBytes();
        mqttConnectOptions.setWill(publishTopic,payload,0,false);
        //mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        addToHistory("Connecting to " + serverUri + "...");

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
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PAHO", "Failed to connect to: " + serverUri +
                            ". Cause: " + ((exception.getCause() == null)?
                            exception.toString() : exception.getCause()));
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("PAHO", "Subscribed to: " + subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PAHO", "Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private void addToHistory(String mainText) {
        System.out.println("LOG: " + mainText);
        mAdapter.add(mainText);
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }

    public void publishMessage(String topic, String publishMessage) {
        MqttMessage message = new MqttMessage();
        message.setPayload(publishMessage.getBytes());
        message.setRetained(false);
        message.setQos(0);
        try {
            mqttAndroidClient.publish(topic, message);
            addToHistory("Message Published");
        } catch (Exception e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
        if (!mqttAndroidClient.isConnected()) {
            addToHistory("Client not connected!");
        }
    }

    public MutableLiveData<Float> getDistanceValue() {
        if (lastReceivedDistance == null) {
            lastReceivedDistance = new MutableLiveData<Float>();
        }
        return lastReceivedDistance;
    }
}
