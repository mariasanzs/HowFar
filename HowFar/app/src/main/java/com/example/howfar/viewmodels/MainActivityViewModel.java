package com.example.howfar.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.howfar.R;

public class MainActivityViewModel extends AndroidViewModel implements SensorEventListener {
    private String userNickname;
    private String joinId;
    private SharedPreferences preferences;
    private String preferencesFile;
    private final String NICKNAMEKEY = "userName";
    private Application application;
    private SensorManager sensorManager;
    private Sensor ProxSensor;
    public boolean appShouldTalk;
    private String APPSHOULDTALKKEY = "appShouldTalk";
    private MutableLiveData<Boolean> proxSensorIsNear;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        ProxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        preferencesFile = application.getString(R.string.shared_preferences_file);
        preferences = application.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        userNickname = preferences.getString(NICKNAMEKEY, "");
        appShouldTalk = preferences.getBoolean(APPSHOULDTALKKEY, false);
        sensorManager.registerListener(this, ProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void nicknameChanged(String newNickname) {
        userNickname = newNickname;
    }
    public String getNickname() {
        return userNickname;
    }

    public void switchChanged(boolean b) {
        appShouldTalk = b;
        if (!b) {
            // If button is disabled, we register sensor so that whenever we put our hand near,
            // the button gets enabled again
            sensorManager.registerListener(this, ProxSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent){
        getSensorStatus();
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] == 0) { //near
                proxSensorIsNear.postValue(Boolean.valueOf(true));
                appShouldTalk = true;
                sensorManager.unregisterListener(this);
            }
        }
    }
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public MutableLiveData<Boolean> getSensorStatus() {
        if (proxSensorIsNear == null) {
            proxSensorIsNear = new MutableLiveData<>(false);
        }
        return proxSensorIsNear;
    }

    public void activityStopped() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NICKNAMEKEY, userNickname);
        editor.putBoolean(APPSHOULDTALKKEY, appShouldTalk);
        editor.apply();
    }

}
