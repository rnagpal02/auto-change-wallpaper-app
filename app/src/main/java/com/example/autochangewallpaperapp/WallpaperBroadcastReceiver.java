package com.example.autochangewallpaperapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WallpaperBroadcastReceiver extends BroadcastReceiver {
    public static final String WALLPAPER_FILENAME_KEY = "wallpaper_filename";
    @Override
    public void onReceive(Context context, Intent intent) {
        String preferencesKey = intent.getStringExtra(WALLPAPER_FILENAME_KEY);
    }
}
