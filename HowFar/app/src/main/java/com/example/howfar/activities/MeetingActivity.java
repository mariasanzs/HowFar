package com.example.howfar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.paho.PahoClient;

import java.util.ArrayList;

public class MeetingActivity  extends AppCompatActivity {
    private PahoClient client;
    private String idMeeting;
    private Handler handler = new Handler();
    private final String topic0 ="distance";
    private final String topic1 ="location";
    private boolean creator = false;
    ArrayList<String> topics;
    Intent intent;
    Runnable runnable;
    int delay = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeeting_activity);
        topics.add(topic0);
        topics.add(topic1);
        intent = getIntent();
        if (intent.getBooleanExtra("meetingCreator",false)){
            creator = true;
            //Create id meeting
            //idMeeting = ...
            //Show id meeting in a textview or whatever to share it with the rest of participants
        } else{
            creator = false;
            //Coger idmeeting de la actividad de join
            //idMeeting=...

        }
        client = new PahoClient(getApplication());

    }
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                Toast.makeText(MeetingActivity.this, "This method is run every 10 seconds",
                        Toast.LENGTH_SHORT).show();
                client.publishMessage(topics.get(0),"distancia");
            }
        }, delay);
        super.onResume();
    }


}
