package com.example.howfar.model;

import android.provider.Telephony;

public class Participant {
    public String nickname;
    public Integer distanceToLocation;

    public Participant(String nickname, Integer distanceToLocation) {
        this.nickname = nickname;
        this.distanceToLocation = distanceToLocation;
    }
}
