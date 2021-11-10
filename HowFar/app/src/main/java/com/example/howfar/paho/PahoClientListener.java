package com.example.howfar.paho;

import com.example.howfar.model.MqttContent;

public interface PahoClientListener {
    void onConnectionCompleted();
    void onConnectionFailed();
    void onMessageArrived();
}
