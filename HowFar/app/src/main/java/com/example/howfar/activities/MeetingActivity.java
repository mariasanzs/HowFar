package com.example.howfar.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.adapter.HistoryAdapter;
import com.example.howfar.paho.PahoClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MeetingActivity  extends AppCompatActivity {
    private PahoClient client;
    private String idMeeting ="02991";
    private Handler handler = new Handler();
    private final String topic0 ="distance";
    private final String topic1 ="location";
    private boolean creator = false;
    HistoryAdapter mAdapter;
    RecyclerView mRecyclerView;
    FloatingActionButton fab;
    ArrayList<String> topics = new ArrayList<>();
    Intent intent;
    Runnable runnable;
    int delay = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_layout);
        fab = findViewById(R.id.fab);
        topics.add(topic0);
        topics.add(topic1);
        intent = getIntent();
        mRecyclerView = findViewById(R.id.history_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HistoryAdapter(new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);

        if (intent.getBooleanExtra("meetingCreator",false)){
            creator = true;
            fab.setVisibility(View.VISIBLE);
            //Create id meeting
            //idMeeting = ...
        } else{
            creator = false;
            fab.setVisibility(View.INVISIBLE);
            //Coger idmeeting de la actividad de join
            //idMeeting=...

        }
        fab.setOnClickListener(view -> sharingId());
        client = new PahoClient(getApplication(),mAdapter,topics);
        //client.subscribeToTopic();

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
    private void sharingId(){
        //FUNCIONA
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Click to share meetingID", idMeeting);
        clipboard.setPrimaryClip(clip);
    }


}
