package com.quexs.tool.utildemo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CameraConfig {
    private SharedPreferences preferences;
    private static CameraConfig instance;

    public CameraConfig(Context context){
        PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isOnlySupportTwoUseCase(){
        return preferences.getBoolean("Two_UseCase", false);
    }

    public void setOnlySupportTwoUseCase(){
        preferences.edit().putBoolean("Two_UseCase", true).apply();
    }
}
