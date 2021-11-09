package com.example.howfar.paho;

public interface PahoClientListener {
    void onConnectionCompleted();
    void onConnectionFailed();
}
