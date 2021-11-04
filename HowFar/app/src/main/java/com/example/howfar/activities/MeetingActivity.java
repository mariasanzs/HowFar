package com.example.howfar.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.howfar.R;
import com.example.howfar.paho.PahoClient;

public class MeetingActivity  extends AppCompatActivity {
    private PahoClient client;
    private String idMeeting;
    private Handler handler = new Handler();
    Runnable runnable;
    int delay = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeeting_activity);
        client = new PahoClient(getApplication(),idMeeting);



    }
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                Toast.makeText(MeetingActivity.this, "This method is run every 10 seconds",
                        Toast.LENGTH_SHORT).show();
                client.publishMessage();
            }
        }, delay);
        super.onResume();
    }


}
