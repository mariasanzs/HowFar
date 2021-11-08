package com.example.howfar.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.howfar.R;
import com.example.howfar.activities.MainActivity;

public class MainActivityViewModel extends AndroidViewModel implements SensorEventListener {
    private String userNickname;
    private SharedPreferences preferences;
    private String preferencesFile;
    private final String NICKNAMEKEY = "userName";
    private Application application;
    //private SwitchCompat bProx;
    private SensorManager sensorManager;
    private Sensor ProxSensor;
    //private boolean ProxSensorIsNear = false;
    private MutableLiveData<Boolean> ProxSensorIsNear;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        ProxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        preferencesFile = application.getString(R.string.shared_preferences_file);
        preferences = application.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        userNickname = preferences.getString(NICKNAMEKEY, "");
        preferences.getString(NICKNAMEKEY, "");
        preferences.getBoolean("ProxSensorIsNear", false);
        //bProx.setChecked(ProxSensorIsNear);
        if(getSensorStatus().getValue()){
            //If the sensor was on, it is still working
            //bProx.setVisibility(View.VISIBLE);
            sensorManager.registerListener(this, ProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            sensorManager.unregisterListener(this, ProxSensor);
            //bProx.setVisibility(View.GONE);
        }

    }

    public void nicknameChanged(String newNickname) {
        userNickname = newNickname;
    }
    public String getNickname() {
        return userNickname;
    }

    public void switchChanged(boolean b) {
        if (!b) {
            // unregister listener and make the appropriate changes in the UI:
            ProxSensorIsNear.setValue(Boolean.valueOf(b));;
            sensorManager.unregisterListener(MainActivityViewModel.this, ProxSensor);
        } else {
            // register listener and make the appropriate changes in the UI:
            sensorManager.registerListener(MainActivityViewModel.this, ProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
            ProxSensorIsNear.setValue(Boolean.valueOf(b));;
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent){
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] == 0) { //near
                ProxSensorIsNear.setValue(Boolean.valueOf(true));
                //bProx.setVisibility(View.VISIBLE);
                //bProx.setChecked(ProxSensorIsNear);
            }
        }
    }


    //public boolean getSensorStatus(){ return this.ProxSensorIsNear; }

    public MutableLiveData<Boolean> getSensorStatus() {
        if (ProxSensorIsNear == null) {
            ProxSensorIsNear = new MutableLiveData<>(false);
        }
        return ProxSensorIsNear;
    }

    public void publishSensorStatus(Boolean bool){
        ProxSensorIsNear = getSensorStatus();
        ProxSensorIsNear.postValue(bool);
    }

    public void activityStopped() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NICKNAMEKEY, userNickname);
        //ProxSensorIsNear = bProx.isChecked();
        editor.putBoolean("ProxSensorIsNear", ProxSensorIsNear.getValue());
        editor.apply();
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
