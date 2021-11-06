package com.example.howfar.model;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttContent {
    public String topic;
    public String payload;

    public MqttContent (String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }
}
