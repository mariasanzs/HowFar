package com.example.howfar.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListPlacesActivity extends AppCompatActivity {
    private static final List<Place> listofcinemas = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private boolean listofitemsinitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_places);
        initListOfCinemas();
        initRecyclerView();

    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this,listofcinemas);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initListOfCinemas() {

        if (listofitemsinitialized == false) {
            try {
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                // fetch JSONObject named employee
                JSONArray graph = obj.getJSONArray("@graph");
                for (int i = 0; i < graph.length(); i++) {
                    // create a JSONObject for fetching single user data
                    JSONObject cinema = graph.getJSONObject(i);
                    String title = cinema.getString("title");
                    JSONObject location = cinema.getJSONObject("location");
                    double latitude = location.getDouble("latitude");
                    double longitude = location.getDouble("longitude");
                    listofcinemas.add(new Place(title,longitude,latitude));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            listofitemsinitialized = true;
        }
    }
    public String loadJSONFromAsset(){
        String json = null;
        try {
            InputStream is = getAssets().open("cinemas.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    }







