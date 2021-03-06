package com.example.howfar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.howfar.R;
import com.example.howfar.adapter.RecyclerViewAdapter;
import com.example.howfar.background.LoadWebContent;
import com.example.howfar.viewmodels.MainActivityViewModel;
import com.example.howfar.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateMeetActivity extends AppCompatActivity implements RecyclerViewAdapter.OnClickListener {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    Executor es;
    Handler handler;
    private List<Place> places = new ArrayList<>();
    private MainActivityViewModel viewModel;
    private boolean listOfCinemasInitialized = false;
    private static final String URL_CINEMAS = "https://datos.madrid.es/egob/catalogo/208862-7650046-ocio_salas.json";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_places);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not suported");
                    }else{
                        //if everything is succesful
                        mTTS.setPitch(0.5f);
                        mTTS.setSpeechRate(0.5f);
                    }
                }else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
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
                    if (viewModel.appShouldTalk) {
                        mTTS.speak("Choose a meeting point", TextToSpeech.QUEUE_FLUSH, null);
                    }
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
        if (!listOfCinemasInitialized) {
            try {
                JSONObject obj = new JSONObject(string_result);
                JSONArray graph = obj.getJSONArray("@graph");
                for (int i = 0; i < graph.length(); i++) {
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
            listOfCinemasInitialized = true;
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
        if (viewModel.appShouldTalk) {
            mTTS.speak("You have selected" + place.getTitle(), TextToSpeech.QUEUE_FLUSH, null);
        }
        Intent intent = new Intent(this, ConfirmMeetActivity.class);
        intent.putExtra("placeName", place.getTitle());
        intent.putExtra("placeLatitude", place.getLatitude());
        intent.putExtra("placeLongitude", place.getLongitude());
        intent.putExtra("nickname", getIntent().getStringExtra("nickname"));
        startActivity(intent);
    }
}









