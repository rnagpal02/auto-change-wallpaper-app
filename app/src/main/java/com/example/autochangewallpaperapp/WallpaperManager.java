package com.example.autochangewallpaperapp;

import static android.content.Context.*;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;

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

    public Bitmap getBitmap(int index) {
        return wallpapers[index].getBitmap();
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
        private Bitmap bitmap;
        private WallpaperTime time;
        private final String preferencesTimeKey;

        public Wallpaper(Context context, WallpaperTime time, int index) {
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;

            if(time != null) {
                setTime(context, time);
            } else {
                recoverTime(context);
            }

            checkImage(context);
        }

        public Wallpaper(Context context, int index) {
            this(context, null, index);
        }

        public void checkImage(Context context) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
            if(!file.exists()) {
                bitmap = null;
            } else {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean downloadWallpaper(Context context, Uri uri) {
            if(uri != null) {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if(bitmap == null) {
                        inputStream.close();
                        return false;
                    }

                    inputStream = context.getContentResolver().openInputStream(uri);
                    ExifInterface exifInterface = new ExifInterface(inputStream);
                    inputStream.close();

                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotation;
                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotation = 270;
                            break;
                        default:
                            rotation = 0;
                            break;
                    }

                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    int imageHeight = bitmap.getHeight();
                    int imageWidth = bitmap.getWidth();
                    float imageAspectRatio = (float)(imageHeight) / (float)(imageWidth);
                    float displayAspectRatio = (float)(context.getResources().getDisplayMetrics().heightPixels) / (float)(context.getResources().getDisplayMetrics().widthPixels);
                    int startHeight = 0, startWidth = 0;

                    // Image is too tall
                    if(imageAspectRatio > displayAspectRatio) {
                        imageHeight = (int)(imageWidth * displayAspectRatio);
                        startHeight = (bitmap.getHeight() - imageHeight) / 2;
                    } // Image is too wide
                    else if(imageAspectRatio < displayAspectRatio) {
                        imageWidth = (int)(imageHeight / displayAspectRatio);
                        startWidth = (bitmap.getWidth() - imageWidth) / 2;
                    }

                    bitmap = Bitmap.createBitmap(bitmap, startWidth, startHeight, imageWidth, imageHeight);
                    this.bitmap = bitmap;

                    File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    outputStream.close();
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        public boolean setWallpaper(Context context) {
            if(bitmap != null) {
                android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(context);
                try {
                    wallpaperManager.setBitmap(bitmap);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        public void clearWallpaper(Context context) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
            file.delete();
            bitmap = null;
        }

        boolean isWallpaperChosen(Context context) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
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

        public Bitmap getBitmap() {
            return bitmap;
        }

        public String getFilename() {
            return filename;
        }
    }
}
