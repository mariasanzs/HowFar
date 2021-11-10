package com.example.howfar.model;

import android.provider.Telephony;

import java.util.Objects;

public class Participant {
    public String nickname;
    public String distanceToLocation;

    public Participant(String nickname, String distanceToLocation) {
        this.nickname = nickname;
        this.distanceToLocation = distanceToLocation;
    }
}
