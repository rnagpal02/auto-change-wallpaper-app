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
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.WindowManager;

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
    private int displayWidth;
    private int displayHeight;

    private WallpaperManager() {}

    public static WallpaperManager getWallpaperManager() {
        return wallpaperManager;
    }

    public void createDefaults(Context context, WallpaperProperties[] defaultWallpaperProperties) {
        setNumWallpapers(context, defaultWallpaperProperties.length);
        wallpapers = new Wallpaper[numWallpapers];
        for(int i = 0; i < numWallpapers; ++i) {
            wallpapers[i] = new Wallpaper(context, i, defaultWallpaperProperties[i]);
        }
        findDisplaySize(context);
    }

    public void initWallpapers(Context context) {
        recoverNumWallpapers(context);
        wallpapers = new Wallpaper[numWallpapers];
        for(int i = 0; i < numWallpapers; ++i) {
            wallpapers[i] = new Wallpaper(context, i);
        }
        findDisplaySize(context);
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

    public boolean uploadWallpaper(Context context, int index, Uri uri) {
        return wallpapers[index].uploadWallpaper(context, uri);
    }

    public boolean downloadWallpaper(int index) {
        return wallpapers[index].downloadWallpaper();
    }

    public boolean setWallpaper(Context context, int index) {
        return wallpapers[index].setWallpaper(context);
    }

    public void clearWallpaper(Context context, int index) {
        wallpapers[index].clearWallpaper(context);
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public String getName(int index) {
        return wallpapers[index].getName();
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

    private void findDisplaySize(Context context) {
        // This gets maximum screen size application may take
        // This may not always provide the accurate display size
        // Getting this varies based on API level
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Rect screen = windowManager.getMaximumWindowMetrics().getBounds();
            displayWidth = screen.width();
            displayHeight = screen.height();
        } else {
            Point point = new Point();
            windowManager.getDefaultDisplay().getRealSize(point);
            displayWidth = point.x;
            displayHeight = point.y;
        }
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
        private final String PREFERENCES_NAME = "/name";

        private final String filename;
        private final String preferencesTimeKey;
        private final String preferencesNameKey;
        private String name;
        private Bitmap bitmap;
        private WallpaperTime time;


        public Wallpaper(Context context, int index, WallpaperProperties properties) {
            filename = FILENAME_PREFIX + index;
            preferencesTimeKey = filename + PREFERENCES_TIME;
            preferencesNameKey = filename + PREFERENCES_NAME;

            if(properties != null) {
                setProperties(context, properties);
            } else {
                recoverProperties(context);
            }

            checkImage(context);
        }

        public Wallpaper(Context context, int index) {
            this(context, index, null);
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

        public boolean uploadWallpaper(Context context, Uri uri) {
            boolean result = false;
            if(uri != null) {
                try {
                    // Get image bitmap from URI
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if(bitmap == null) {
                        inputStream.close();
                        return false;
                    }

                    // get EXIF data from image
                    inputStream = context.getContentResolver().openInputStream(uri);
                    ExifInterface exifInterface = new ExifInterface(inputStream);
                    inputStream.close();

                    // Find rotation (in degrees) of image using EXIF data
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

                    // Rotate bitmap using rotation matrix
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                    // Find aspect ratios
                    int imageHeight = bitmap.getHeight();
                    int imageWidth = bitmap.getWidth();
                    float imageAspectRatio = (float)(imageHeight) / (float)(imageWidth);

                    float displayAspectRatio = (float)(getDisplayHeight()) / (float)(getDisplayWidth());
                    int startHeight = 0, startWidth = 0;

                    // Image is too tall, crop top and bottom
                    if(imageAspectRatio > displayAspectRatio) {
                        imageHeight = (int)(imageWidth * displayAspectRatio);
                        startHeight = (bitmap.getHeight() - imageHeight) / 2;
                    } // Image is too wide, crop left and right
                    else if(imageAspectRatio < displayAspectRatio) {
                        imageWidth = (int)(imageHeight / displayAspectRatio);
                        startWidth = (bitmap.getWidth() - imageWidth) / 2;
                    }

                    // Perform crop
                    bitmap = Bitmap.createBitmap(bitmap, startWidth, startHeight, imageWidth, imageHeight);
                    this.bitmap = bitmap;

                    // Download image to local app-specific external storage
                    File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    result = bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    outputStream.close();
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        public boolean downloadWallpaper() {
            boolean result = false;
            if(bitmap != null) {
                try {
                    // Download to public downloads directory
                    File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if(!downloadDirectory.exists()) {
                        downloadDirectory.mkdirs();
                    }
                    File downloadFile = new File(downloadDirectory, name + ".png");
                    FileOutputStream outputStream = new FileOutputStream(downloadFile);
                    result = bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
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

        public String getName() {
            return name;
        }

        public WallpaperTime getTime() {
            return time;
        }

        public void setTime(Context context, WallpaperTime time) {
            this.time = time;
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(preferencesTimeKey, time.getTimeMinutes());
            editor.apply();
        }

        public void setProperties(Context context, WallpaperProperties properties) {
            this.name = properties.getName();
            this.time = properties.getTime();
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(preferencesNameKey, name);
            editor.putInt(preferencesTimeKey, time.getTimeMinutes());
            editor.apply();
        }


        public void recoverProperties(Context context) {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILENAME, MODE_PRIVATE);
            String default_str = "";
            int default_int = 0;
            name = preferences.getString(preferencesNameKey, default_str);
            int minutes = preferences.getInt(preferencesTimeKey, default_int);
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
