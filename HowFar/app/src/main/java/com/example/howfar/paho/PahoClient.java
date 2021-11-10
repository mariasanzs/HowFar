package com.example.howfar.paho;

import android.app.Application;
import android.util.Log;

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
    MqttAndroidClient mqttAndroidClient;
    String clientId;
    public ArrayList<MqttContent> messagesQueue;
    ArrayList<String> clientTopics;
    public PahoClient(Application application, ArrayList<String> topics, PahoClientListener listener) {
        this.listener = listener;
        this.messagesQueue = new ArrayList<>();
        clientId = clientId + System.currentTimeMillis();
        clientTopics = topics;
        mqttAndroidClient = new MqttAndroidClient(application.getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                    Log.d("HOWFARLOG","Paho Client: Reconnecting");
                    for (String topic : clientTopics) {
                        subscribeToTopic(topic);
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("HOWFARLOG","Paho Client: Connection Lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                MqttContent mqttContent = new MqttContent(topic, payload);
                Log.d("HOWFARLOG","Received message from " + topic + payload);
                messagesQueue.add(mqttContent);
                listener.onMessageArrived();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    listener.onConnectionCompleted();
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    for (String topic : clientTopics) {
                        subscribeToTopic(topic);
                    }
                    Log.d("HOWFARLOG","Paho Client: Connection completed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("HOWFARLOG", "Failed to connect to: " + serverUri +
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
                    Log.d("HOWFARLOG","Subscribed to " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("HOWFARLOG","Failed to subsribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String publishMessage, boolean retain) {
        MqttMessage message = new MqttMessage();
        message.setPayload(publishMessage.getBytes());
        message.setRetained(retain);
        message.setQos(1);
        try {
            mqttAndroidClient.publish(topic,message);
            Log.d("HOWFARLOG","Published message to topic. Message: " + message.getPayload().toString() + " Topic: " + topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected()  {
        return mqttAndroidClient.isConnected();
    }

    public void disconnect() {
        try {
            Log.d("HOWFARLOG","Paho Client: Disconnected");
            mqttAndroidClient.disconnect();
            listener = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
