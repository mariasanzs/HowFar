package com.example.howfar.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.howfar.R;

public class MainActivityViewModel extends AndroidViewModel {
    private String userNickname;
    private String joinId;
    private SharedPreferences preferences;
    private String preferencesFile;
    private final String NICKNAMEKEY = "userName";

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        preferencesFile = application.getString(R.string.shared_preferences_file);
        preferences = application.getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        userNickname = preferences.getString(NICKNAMEKEY, "");
        preferences.getString(NICKNAMEKEY, "");
    }

    public void nicknameChanged(String newNickname) {
        userNickname = newNickname;
    }
    public String getNickname() {
        return userNickname;
    }
    public void activityStopped() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(NICKNAMEKEY, userNickname);
        editor.apply();
    }
}
