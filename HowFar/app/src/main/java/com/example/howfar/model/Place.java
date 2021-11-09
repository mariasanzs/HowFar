package com.example.howfar.model;

import java.io.Serializable;

public class Place implements Serializable {
    private String title;
    private double longitude;
    private double latitude;


    public Place(String title, double longitude, double latitude) {
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTitle() {
        return title;
    }
    public double getLongitude() {
        return longitude;
    }
    public  double getLatitude(){
        return  latitude;
     }
}
