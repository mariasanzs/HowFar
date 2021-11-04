package com.example.howfar.paho;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

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
import java.util.Calendar;
import java.util.Date;

public class PahoClient {
    final String serverUri = "tcp://192.168.80.1:1883"; //Cambiar URL
    String subscriptionTopic ;
    String publishTopic;
    String publishMessage;
    String lastWillMessage = "Goodbye!";
    MqttAndroidClient mqttAndroidClient;
    String clientId;
    private MutableLiveData<Float> lastReceivedDistance;
    private HistoryAdapter mAdapter;

    public PahoClient(Application application,String id_meeting) {
        subscriptionTopic = id_meeting;
        publishTopic = id_meeting;
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

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
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

    public void publishMessage() {
        MqttMessage message = new MqttMessage();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        publishMessage = strDate;
        message.setPayload(publishMessage.getBytes());
        message.setRetained(false);
        message.setQos(0);
        try {
            mqttAndroidClient.publish(publishTopic, message);
            addToHistory("Message Published");
        } catch (Exception e) {
            e.printStackTrace();
            addToHistory(e.toString());
        }
        if (!mqttAndroidClient.isConnected()) {
            addToHistory("Client not connected!");
        }
    }

    public MutableLiveData<Float> getLightValue() {
        if (lastReceivedDistance == null) {
            lastReceivedDistance = new MutableLiveData<Float>();
        }
        return lastReceivedDistance;
    }
}
