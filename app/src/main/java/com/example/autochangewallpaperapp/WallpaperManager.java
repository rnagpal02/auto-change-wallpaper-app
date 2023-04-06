package com.example.autochangewallpaperapp;

import static android.content.Context.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class WallpaperManager extends BroadcastReceiver {
    public static final String EXTRA_TARGET_WALLPAPER_KEY = "target_wallpaper";
    private final String PREFERENCES_FILENAME = "wallpaper_manager";
    private final String PREFERENCES_NUM_WALLPAPERS = "num_wallpapers";

    private static WallpaperManager wallpaperManager = new WallpaperManager();

    private Wallpaper[] wallpapers;
    private int numWallpapers;

    private WallpaperManager() {}

    public static WallpaperManager getWallpaperManager() {
        return wallpaperManager;
    }

    public void createDefaults(Context context, WallpaperTime[] defaultWallpaperTimes) {
        setNumWallpapers(context, defaultWallpaperTimes.length);
        wallpapers = new Wallpaper[numWallpapers];
        for(int i = 0; i < numWallpapers; ++i) {
            wallpapers[i] = new Wallpaper(context, defaultWallpaperTimes[i], i);
        }
    }

    public void initWallpapers(Context context) {
        recoverNumWallpapers(context);
        wallpapers = new Wallpaper[numWallpapers];
        for(int i = 0; i < numWallpapers; ++i) {
            wallpapers[i] = new Wallpaper(context, i);
        }
    }

    public boolean isWallpaperChosen(Context context, int index) {
        return wallpapers[index].isWallpaperChosen(context);
    }

    public boolean downloadWallpaper(Context context, int index, Uri uri) {
        return wallpapers[index].downloadWallpaper(context, uri);
    }

    public boolean setWallpaper(Context context, int index) {
        return wallpapers[index].setWallpaper(context);
    }

    public void clearWallpaper(Context context, int index) {
        wallpapers[index].clearWallpaper(context);
    }

    public WallpaperTime getTime(int index) {
        return wallpapers[index].getTime();
    }

    public void setTime(Context context, int index, WallpaperTime time) {
        wallpapers[index].setTime(context, time);
    }

    public String getFilename(int index) {
        return wallpapers[index].getFilename();
    }

    private void setNumWallpapers(Context context, int numWallpapers) {
        this.numWallpapers = numWallpapers;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREFERENCES_NUM_WALLPAPERS, numWallpapers);
        editor.apply();
    }

    private void recoverNumWallpapers(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
        int default_value = 0;
        numWallpapers = sharedPreferences.getInt(PREFERENCES_NUM_WALLPAPERS, default_value);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO
    }

    private class Wallpaper {
        private final String FILENAME_PREFIX = "wallpaper_";
        private final String PREFERENCES_TIME = "/time";

        private String filename;
        private int index;
        private WallpaperTime time;
        private String preferencesTimeKey;

        public Wallpaper(Context context, WallpaperTime time, int index) {
            this.index = index;
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;
            setTime(context, time);
        }

        public Wallpaper(Context context, int index) {
            this.index = index;
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;
            recoverTime(context);
        }

        public boolean downloadWallpaper(Context context, Uri uri) {
            if(uri != null) {
                try {
                    InputStream inputStream = (FileInputStream) context.getContentResolver().openInputStream(uri);
                    FileOutputStream outputStream = context.openFileOutput(filename, MODE_PRIVATE);

                    byte[] buffer = new byte[1024*4];
                    while(inputStream.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }

                    inputStream.close();
                    outputStream.close();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        public boolean setWallpaper(Context context) {
            try {
                File file = context.getFileStreamPath(filename);
                FileInputStream inputStream = new FileInputStream(file);
                android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(context);
                wallpaperManager.setStream(inputStream);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public void clearWallpaper(Context context) {
            File file = context.getFileStreamPath(filename);
            file.delete();
        }

        boolean isWallpaperChosen(Context context) {
            File file = context.getFileStreamPath(filename);
            return file.exists();
        }

        public WallpaperTime getTime() {
            return time;
        }

        public void setTime(Context context, WallpaperTime time) {
            this.time = time;
            int minutes = time.getTimeMinutes();
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(preferencesTimeKey, minutes);
            editor.apply();
        }

        public void recoverTime(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
            int default_value = 0;
            int minutes = preferences.getInt(preferencesTimeKey, default_value);
            time = new WallpaperTime(minutes);
        }

        public String getFilename() {
            return filename;
        }
    }
}
