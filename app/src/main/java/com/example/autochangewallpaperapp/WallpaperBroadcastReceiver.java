package com.example.autochangewallpaperapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WallpaperBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WallpaperManager.getWallpaperManager().onReceiveBroadcast(context, intent);
    }
}
