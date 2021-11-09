package com.example.howfar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.adapter.RecyclerViewAdapter;
import com.example.howfar.background.LoadWebContent;
import com.example.howfar.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateMeetActivity extends AppCompatActivity implements RecyclerViewAdapter.OnClickListener {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    Executor es;
    Handler handler;
    private List<Place> places = new ArrayList<>();
    private boolean listofcinemasinitialized = false;
    public static final String LOGSLOADWEBCONTENT = "LOGSLOADWEBCONTENT";
    // To load content from the website
    private static final String URL_CINEMAS = "https://datos.madrid.es/egob/catalogo/208862-7650046-ocio_salas.json";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_places);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.mipmap.ic_launcher);
        progressDialog.setMessage("Loading...");
        es = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                String string_result;
                super.handleMessage(msg);
                string_result = msg.getData().getString("text");
                if (string_result != null && string_result != "") {
                    initCreateMeetActivity(string_result);
                    initRecyclerView();
                    progressDialog.dismiss();
                } else {
                    Toast.makeText(CreateMeetActivity.this,
                            "It was not possible to get content from the web",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        };
        LoadWebContent loadURLContentsjson = new LoadWebContent(handler, CONTENT_TYPE_JSON, URL_CINEMAS);
        es.execute(loadURLContentsjson);
        progressDialog.show();
    }

    private void initCreateMeetActivity(String string_result){
        if (listofcinemasinitialized == false) {
            try {
                JSONObject obj = new JSONObject(string_result);
                // fetch JSONObject named employee
                JSONArray graph = obj.getJSONArray("@graph");
                for (int i = 0; i < graph.length(); i++) {
                    // create a JSONObject for fetching single user data
                    JSONObject cinema = graph.getJSONObject(i);
                    String title = cinema.getString("title");
                    JSONObject location = cinema.getJSONObject("location");
                    double latitude = location.getDouble("latitude");
                    double longitude = location.getDouble("longitude");
                    places.add(new Place(title,longitude,latitude));
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(CreateMeetActivity.this,
                        "It was not possible to get content from the web",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            listofcinemasinitialized = true;
        }
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this, places, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(int position) {
        Place place = places.get(position);
        Intent intent = new Intent(this, ConfirmMeetActivity.class);
        intent.putExtra("placeName", place.getTitle());
        intent.putExtra("placeLatitude", place.getLatitude());
        intent.putExtra("placeLongitude", place.getLongitude());
        intent.putExtra("nickname", getIntent().getStringExtra("nickname"));
        startActivity(intent);
    }
}









