package com.example.autochangewallpaperapp;

import static android.content.Context.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class WallpaperManager {
    public static final String EXTRA_TARGET_WALLPAPER_KEY = "target_wallpaper";
    private final String PREFERENCES_FILENAME = "wallpaper_manager";
    private final String PREFERENCES_NUM_WALLPAPERS = "num_wallpapers";

    private static final WallpaperManager wallpaperManager = new WallpaperManager();

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

    public boolean startAutoChange(Context context) {
        // Make sure all wallpapers have been chosen
        for(int i = 0; i < numWallpapers; ++i) {
            boolean wallpaperChosen = isWallpaperChosen(context, i);
            if(!wallpaperChosen) {
                return false;
            }
        }

        // Find the closest upcoming wallpaper time-wise
        Calendar targetDate = Calendar.getInstance();
        int currentMinutes = targetDate.get(Calendar.HOUR_OF_DAY) * 60 + targetDate.get(Calendar.MINUTE);
        int targetWallpaper = -1;
        WallpaperTime targetTime = wallpaperManager.getTime(0);
        for(int i = 0; i < numWallpapers; ++i) {
            WallpaperTime wallpaperTime = wallpaperManager.getTime(i);
            int wallpaperMinutes = wallpaperTime.getTimeMinutes();
            if(wallpaperMinutes > currentMinutes) {
                targetWallpaper = i;
                targetTime = wallpaperTime;
                break;
            }
        }
        // If the target wasn't found above, the next one is the first wallpaper on the next day
        if(targetWallpaper < 0) {
            targetDate.add(Calendar.DATE, 1);
            targetWallpaper = 0;
        }

        // Set the previous wallpaper to maintain the cycle
        int previousWallpaper = targetWallpaper - 1;
        if(previousWallpaper < 0) {
            previousWallpaper = numWallpapers - 1;
        }
        wallpapers[previousWallpaper].setWallpaper(context);

        // Set Calendar values
        targetDate.set(Calendar.HOUR_OF_DAY, targetTime.hour);
        targetDate.set(Calendar.MINUTE, targetTime.minute);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

        long targetTimeMillis = targetDate.getTimeInMillis();
        setAlarm(context, targetTimeMillis, targetWallpaper);

        return true;
    }

    public void stopAutoChange(Context context) {
        cancelAlarm(context);
    }

    public void onReceiveBroadcast(Context context, Intent intent) {
        // Set wallpaper
        int default_value = -1;
        int targetWallpaperIndex = intent.getIntExtra(EXTRA_TARGET_WALLPAPER_KEY, default_value);
        Wallpaper wallpaper = new Wallpaper(context, targetWallpaperIndex);
        wallpaper.setWallpaper(context);

        // Get info about next wallpaper
        recoverNumWallpapers(context);
        Calendar nextAlarmDate = Calendar.getInstance();
        int nextWallpaperIndex = targetWallpaperIndex + 1;
        if(nextWallpaperIndex >= numWallpapers) {
            nextWallpaperIndex = 0;
            nextAlarmDate.add(Calendar.DATE, 1);
        }

        // Set date of next wallpaper
        Wallpaper nextWallpaper = new Wallpaper(context, nextWallpaperIndex);
        WallpaperTime nextTime = nextWallpaper.getTime();
        nextAlarmDate.set(Calendar.HOUR_OF_DAY, nextTime.hour);
        nextAlarmDate.set(Calendar.MINUTE, nextTime.minute);
        nextAlarmDate.set(Calendar.SECOND, 0);
        nextAlarmDate.set(Calendar.MILLISECOND, 0);

        // Start new alarm
        long nextAlarmTimeMillis = nextAlarmDate.getTimeInMillis();
        setAlarm(context, nextAlarmTimeMillis, nextWallpaperIndex);
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

    public Bitmap getBitmap(Context context, int index) {
        return wallpapers[index].getBitmap(context);
    }

    public String getFilename(int index) {
        return wallpapers[index].getFilename();
    }

    public int getNumWallpapers() {
        return numWallpapers;
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

    private void setAlarm(Context context, long timeMillis, int wallpaper) {
        Intent intent = getBroadcastIntent(context);
        intent.putExtra(WallpaperManager.EXTRA_TARGET_WALLPAPER_KEY, wallpaper);
        PendingIntent pendingIntent = getBroadcastPendingIntent(context, intent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, timeMillis, pendingIntent);
    }

    private void cancelAlarm(Context context) {
        Intent intent = getBroadcastIntent(context);
        PendingIntent pendingIntent = getBroadcastPendingIntent(context, intent);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private Intent getBroadcastIntent(Context context) {
        return new Intent(context, WallpaperBroadcastReceiver.class);
    }

    private PendingIntent getBroadcastPendingIntent(Context context, Intent intent) {
        return PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private class Wallpaper {
        private final String FILENAME_PREFIX = "wallpaper_";
        private final String PREFERENCES_TIME = "/time";

        private final String filename;
        private WallpaperTime time;
        private final String preferencesTimeKey;

        public Wallpaper(Context context, WallpaperTime time, int index) {
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;
            setTime(context, time);
        }

        public Wallpaper(Context context, int index) {
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;
            recoverTime(context);
        }

        public boolean downloadWallpaper(Context context, Uri uri) {
            if(uri != null) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
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

        public Bitmap getBitmap(Context context) {
            String absolutePath = context.getFileStreamPath(filename).getAbsolutePath();
            Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
            return bitmap;
        }

        public String getFilename() {
            return filename;
        }
    }
}
