package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String FIRST_RUN_KEY = "first_run";

    private final String TARGET_WALLPAPER_FILENAME = "target_wallpaper";
    private final String MORNING_WALLPAPER_FILENAME = "morning_wallpaper";
    private final String AFTERNOON_WALLPAPER_FILENAME = "afternoon_wallpaper";
    private final String EVENING_WALLPAPER_FILENAME = "evening_wallpaper";
    private final String NIGHT_WALLPAPER_FILENAME = "night_wallpaper";

    private final String MORNING_TIME_HOUR_KEY = "morning_hour";
    private final String MORNING_TIME_MINUTE_KEY = "morning_min";
    private final String AFTERNOON_TIME_HOUR_KEY = "afternoon_hour";
    private final String AFTERNOON_TIME_MINUTE_KEY = "afternoon_min";
    private final String EVENING_TIME_HOUR_KEY = "evening_hour";
    private final String EVENING_TIME_MINUTE_KEY = "evening_min";
    private final String NIGHT_TIME_HOUR_KEY = "night_hour";
    private final String NIGHT_TIME_MINUTE_KEY = "night_min";

    private final String AUTO_CHANGE_KEY = "auto_change";

    private final int MORNING_TIME_HOUR_DEFAULT = 7;
    private final int MORNING_TIME_MINUTE_DEFAULT = 0;
    private final int AFTERNOON_TIME_HOUR_DEFAULT = 12;
    private final int AFTERNOON_TIME_MINUTE_DEFAULT = 0;
    private final int EVENING_TIME_HOUR_DEFAULT = 16;
    private final int EVENING_TIME_MINUTE_DEFAULT = 0;
    private final int NIGHT_TIME_HOUR_DEFAULT = 19;
    private final int NIGHT_TIME_MINUTE_DEFAULT = 0;

    private TextView morningWallpaperText;
    private TextView afternoonWallpaperText;
    private TextView eveningWallpaperText;
    private TextView nightWallpaperText;

    private Button morningWallpaperClear;
    private Button afternoonWallpaperClear;
    private Button eveningWallpaperClear;
    private Button nightWallpaperClear;

    private Button morningWallpaperPreview;
    private Button afternoonWallpaperPreview;
    private Button eveningWallpaperPreview;
    private Button nightWallpaperPreview;

    private Button morningWallpaperSet;
    private Button afternoonWallpaperSet;
    private Button eveningWallpaperSet;
    private Button nightWallpaperSet;

    private Button morningWallpaperTime;
    private Button afternoonWallpaperTime;
    private Button eveningWallpaperTime;
    private Button nightWallpaperTime;

    private Switch autoChangeWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.morningWallpaperChoose).setOnClickListener(wallpaperChooseListener);
        findViewById(R.id.afternoonWallpaperChoose).setOnClickListener(wallpaperChooseListener);
        findViewById(R.id.eveningWallpaperChoose).setOnClickListener(wallpaperChooseListener);
        findViewById(R.id.nightWallpaperChoose).setOnClickListener(wallpaperChooseListener);

        morningWallpaperText = findViewById(R.id.morningWallpaperText);
        afternoonWallpaperText = findViewById(R.id.afternoonWallpaperText);
        eveningWallpaperText = findViewById(R.id.eveningWallpaperText);
        nightWallpaperText = findViewById(R.id.nightWallpaperText);

        morningWallpaperClear = findViewById(R.id.morningWallpaperClear);
        afternoonWallpaperClear = findViewById(R.id.afternoonWallpaperClear);
        eveningWallpaperClear = findViewById(R.id.eveningWallpaperClear);
        nightWallpaperClear = findViewById(R.id.nightWallpaperClear);

        morningWallpaperPreview = findViewById(R.id.morningWallpaperPreview);
        afternoonWallpaperPreview = findViewById(R.id.afternoonWallpaperPreview);
        eveningWallpaperPreview = findViewById(R.id.eveningWallpaperPreview);
        nightWallpaperPreview = findViewById(R.id.nightWallpaperPreview);

        morningWallpaperSet = findViewById(R.id.morningWallpaperSet);
        afternoonWallpaperSet = findViewById(R.id.afternoonWallpaperSet);
        eveningWallpaperSet = findViewById(R.id.eveningWallpaperSet);
        nightWallpaperSet = findViewById(R.id.nightWallpaperSet);

        morningWallpaperTime = findViewById(R.id.morningWallpaperTime);
        afternoonWallpaperTime = findViewById(R.id.afternoonWallpaperTime);
        eveningWallpaperTime = findViewById(R.id.eveningWallpaperTime);
        nightWallpaperTime = findViewById(R.id.nightWallpaperTime);

        autoChangeWallpaper = findViewById(R.id.autoChangeSwitch);

        morningWallpaperClear.setOnClickListener(wallpaperClearListener);
        afternoonWallpaperClear.setOnClickListener(wallpaperClearListener);
        eveningWallpaperClear.setOnClickListener(wallpaperClearListener);
        nightWallpaperClear.setOnClickListener(wallpaperClearListener);

        morningWallpaperPreview.setOnClickListener(wallpaperPreviewListener);
        afternoonWallpaperPreview.setOnClickListener(wallpaperPreviewListener);
        eveningWallpaperPreview.setOnClickListener(wallpaperPreviewListener);
        nightWallpaperPreview.setOnClickListener(wallpaperPreviewListener);

        morningWallpaperSet.setOnClickListener(wallpaperSetListener);
        afternoonWallpaperSet.setOnClickListener(wallpaperSetListener);
        eveningWallpaperSet.setOnClickListener(wallpaperSetListener);
        nightWallpaperSet.setOnClickListener(wallpaperSetListener);

        morningWallpaperTime.setOnClickListener(wallpaperTimeListener);
        afternoonWallpaperTime.setOnClickListener(wallpaperTimeListener);
        eveningWallpaperTime.setOnClickListener(wallpaperTimeListener);
        nightWallpaperTime.setOnClickListener(wallpaperTimeListener);

        autoChangeWallpaper.setOnCheckedChangeListener(autoChangeListener);

        checkFirstRun();
        updateUI();
    }

    private void checkFirstRun() {
        final String TAG = "CHECK_FIRST_RUN";

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean is_first_run = preferences.getBoolean(FIRST_RUN_KEY, true);
        if(is_first_run) {
            Log.d(TAG, "First run");
            SharedPreferences.Editor editor = preferences.edit();

            editor.putInt(MORNING_TIME_HOUR_KEY, MORNING_TIME_HOUR_DEFAULT);
            editor.putInt(MORNING_TIME_MINUTE_KEY, MORNING_TIME_MINUTE_DEFAULT);
            editor.putInt(AFTERNOON_TIME_HOUR_KEY, AFTERNOON_TIME_HOUR_DEFAULT);
            editor.putInt(AFTERNOON_TIME_MINUTE_KEY, AFTERNOON_TIME_MINUTE_DEFAULT);
            editor.putInt(EVENING_TIME_HOUR_KEY, EVENING_TIME_HOUR_DEFAULT);
            editor.putInt(EVENING_TIME_MINUTE_KEY, EVENING_TIME_MINUTE_DEFAULT);
            editor.putInt(NIGHT_TIME_HOUR_KEY, NIGHT_TIME_HOUR_DEFAULT);
            editor.putInt(NIGHT_TIME_MINUTE_KEY, NIGHT_TIME_MINUTE_DEFAULT);

            editor.putBoolean(AUTO_CHANGE_KEY, false);

            editor.putBoolean(FIRST_RUN_KEY, false);
            editor.apply();
            Log.d(TAG, "Set default wallpaper times");
        }
    }

    private void updateUI() {
        boolean morningWallpaperChosen = isFileExists(MORNING_WALLPAPER_FILENAME);
        boolean afternoonWallpaperChosen = isFileExists(AFTERNOON_WALLPAPER_FILENAME);
        boolean eveningWallpaperChosen = isFileExists(EVENING_WALLPAPER_FILENAME);
        boolean nightWallpaperChosen = isFileExists(NIGHT_WALLPAPER_FILENAME);

        morningWallpaperText.setVisibility(morningWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        afternoonWallpaperText.setVisibility(afternoonWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        eveningWallpaperText.setVisibility(eveningWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        nightWallpaperText.setVisibility(nightWallpaperChosen ? View.INVISIBLE : View.VISIBLE);

        morningWallpaperClear.setVisibility(morningWallpaperChosen ? View.VISIBLE : View.GONE);
        afternoonWallpaperClear.setVisibility(afternoonWallpaperChosen ? View.VISIBLE : View.GONE);
        eveningWallpaperClear.setVisibility(eveningWallpaperChosen ? View.VISIBLE : View.GONE);
        nightWallpaperClear.setVisibility(nightWallpaperChosen ? View.VISIBLE : View.GONE);

        morningWallpaperPreview.setClickable(morningWallpaperChosen);
        afternoonWallpaperPreview.setClickable(afternoonWallpaperChosen);
        eveningWallpaperPreview.setClickable(eveningWallpaperChosen);
        nightWallpaperPreview.setClickable(nightWallpaperChosen);

        morningWallpaperSet.setClickable(morningWallpaperChosen);
        afternoonWallpaperSet.setClickable(afternoonWallpaperChosen);
        eveningWallpaperSet.setClickable(eveningWallpaperChosen);
        nightWallpaperSet.setClickable(nightWallpaperChosen);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        autoChangeWallpaper.setChecked(preferences.getBoolean(AUTO_CHANGE_KEY, false));
    }

    private boolean isFileExists(String filename) {
        File file = getFileStreamPath(filename);
        return file.exists();
    }

    private final View.OnClickListener wallpaperChooseListener = new View.OnClickListener() {
        public void onClick(View v) {
            final String TAG = "WALLPAPER_CHOOSE_LISTENER";

            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperChoose)) {
                targetWallpaper = MORNING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.afternoonWallpaperChoose)) {
                targetWallpaper = AFTERNOON_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.eveningWallpaperChoose)) {
                targetWallpaper = EVENING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.nightWallpaperChoose)) {
                targetWallpaper = NIGHT_WALLPAPER_FILENAME;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TARGET_WALLPAPER_FILENAME, targetWallpaper);
            editor.apply();
            chooseWallpaper.launch("image/*");
        }
    };

    private final ActivityResultLauncher<String> chooseWallpaper = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    final String TAG = "CHOOSE_WALLPAPER_ACTIVITY_RESULT";

                    if(result != null) {
                        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                        String default_str = "default";
                        String targetWallpaper = preferences.getString(TARGET_WALLPAPER_FILENAME, default_str);
                        if (!targetWallpaper.equals(default_str)) {
                            try {
                                InputStream inputStream = (FileInputStream) getApplicationContext().getContentResolver().openInputStream(result);
                                FileOutputStream outputStream = openFileOutput(targetWallpaper, MODE_PRIVATE);

                                byte[] buffer = new byte[1024*4];
                                while(inputStream.read(buffer) != -1) {
                                    outputStream.write(buffer);
                                }

                                inputStream.close();
                                outputStream.close();
                                updateUI();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Error downloading wallpaper", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Target wallpaper not found");
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No Wallpaper Chosen", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final View.OnClickListener wallpaperClearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_CLEAR_LISTENER";

            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperClear)) {
                targetWallpaper = MORNING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.afternoonWallpaperClear)) {
                targetWallpaper = AFTERNOON_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.eveningWallpaperClear)) {
                targetWallpaper = EVENING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.nightWallpaperClear)) {
                targetWallpaper = NIGHT_WALLPAPER_FILENAME;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            File file = getFileStreamPath(targetWallpaper);
            file.delete();
            updateUI();
        }
    };

    private final View.OnClickListener wallpaperPreviewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_PREVIEW_LISTENER";

            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperPreview)) {
                targetWallpaper = MORNING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.afternoonWallpaperPreview)) {
                targetWallpaper = AFTERNOON_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.eveningWallpaperPreview)) {
                targetWallpaper = EVENING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.nightWallpaperPreview)) {
                targetWallpaper = NIGHT_WALLPAPER_FILENAME;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            Intent intent = new Intent(getApplicationContext(), WallpaperPreview.class);
            intent.putExtra(WallpaperPreview.WALLPAPER_FILENAME_KEY, targetWallpaper);
            startActivity(intent);
        }
    };

    private final View.OnClickListener wallpaperSetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_SET_LISTENER";

            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperSet)) {
                targetWallpaper = MORNING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.afternoonWallpaperSet)) {
                targetWallpaper = AFTERNOON_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.eveningWallpaperSet)) {
                targetWallpaper = EVENING_WALLPAPER_FILENAME;
            } else if(v == findViewById(R.id.nightWallpaperSet)) {
                targetWallpaper = NIGHT_WALLPAPER_FILENAME;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            try {
                File file = getFileStreamPath(targetWallpaper);
                FileInputStream inputStream = new FileInputStream(file);
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                wallpaperManager.setStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Unable to set wallpaper", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final View.OnClickListener wallpaperTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String TAG = "WALLPAPER_TIME_LISTENER";

            String hour_key;
            String min_key;

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);

            if(view == findViewById(R.id.morningWallpaperTime)) {
                hour_key = MORNING_TIME_HOUR_KEY;
                min_key = MORNING_TIME_MINUTE_KEY;
            } else if(view == findViewById(R.id.afternoonWallpaperTime)) {
                hour_key = AFTERNOON_TIME_HOUR_KEY;
                min_key = AFTERNOON_TIME_MINUTE_KEY;
            } else if(view == findViewById(R.id.eveningWallpaperTime)) {
                hour_key = EVENING_TIME_HOUR_KEY;
                min_key = EVENING_TIME_MINUTE_KEY;
            } else if(view == findViewById(R.id.nightWallpaperTime)) {
                hour_key = NIGHT_TIME_HOUR_KEY;
                min_key = NIGHT_TIME_MINUTE_KEY;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            int default_val = -1;
            int current_hour = preferences.getInt(hour_key, default_val);
            int current_min = preferences.getInt(min_key, default_val);

            if(current_hour < 0 || current_min < 0) {
                Log.e(TAG, "Error getting currently set times");
                return;
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(hour_key, hour);
                    editor.putInt(min_key, minute);
                    editor.apply();
                }
            }, current_hour, current_min, false);
            timePickerDialog.show();
        }
    };

    private final CompoundButton.OnCheckedChangeListener autoChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked) {
                boolean morningWallpaperChosen = isFileExists(MORNING_WALLPAPER_FILENAME);
                boolean afternoonWallpaperChosen = isFileExists(AFTERNOON_WALLPAPER_FILENAME);
                boolean eveningWallpaperChosen = isFileExists(EVENING_WALLPAPER_FILENAME);
                boolean nightWallpaperChosen = isFileExists(NIGHT_WALLPAPER_FILENAME);

                if(!morningWallpaperChosen || !afternoonWallpaperChosen || !eveningWallpaperChosen || !nightWallpaperChosen) {
                    Toast.makeText(getApplicationContext(), "Finish choosing all wallpapers", Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(false);
                    return;
                }

                int default_val = -1;
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                int morningHour = preferences.getInt(MORNING_TIME_HOUR_KEY, default_val);
                int morningMin = preferences.getInt(MORNING_TIME_MINUTE_KEY, default_val);
                int afternoonHour = preferences.getInt(AFTERNOON_TIME_HOUR_KEY, default_val);
                int afternoonMin = preferences.getInt(AFTERNOON_TIME_MINUTE_KEY, default_val);
                int eveningHour = preferences.getInt(EVENING_TIME_HOUR_KEY, default_val);
                int eveningMin = preferences.getInt(EVENING_TIME_MINUTE_KEY, default_val);
                int nightHour = preferences.getInt(NIGHT_TIME_HOUR_KEY, default_val);
                int nightMin = preferences.getInt(NIGHT_TIME_MINUTE_KEY, default_val);

                Calendar currentDate = Calendar.getInstance();
                Calendar morningDate = Calendar.getInstance();
                morningDate.set(Calendar.SECOND, 0);
                morningDate.set(Calendar.MILLISECOND, 0);
                Calendar afternoonDate = morningDate;
                Calendar eveningDate = morningDate;
                Calendar nightDate = morningDate;

                morningDate.set(Calendar.HOUR, morningHour);
                morningDate.set(Calendar.MINUTE, morningMin);
                afternoonDate.set(Calendar.HOUR, afternoonHour);
                afternoonDate.set(Calendar.MINUTE, afternoonMin);
                eveningDate.set(Calendar.HOUR, eveningHour);
                eveningDate.set(Calendar.MINUTE, eveningMin);
                nightDate.set(Calendar.HOUR, nightHour);
                nightDate.set(Calendar.MINUTE, nightMin);

                if(currentDate.after(morningDate)) {
                    morningDate.add(Calendar.DATE, 1);
                }
                if(currentDate.after(afternoonDate)) {
                    afternoonDate.add(Calendar.DATE, 1);
                }
                if(currentDate.after(eveningDate)) {
                    eveningDate.add(Calendar.DATE, 1);
                }
                if(currentDate.after(nightDate)) {
                    nightDate.add(Calendar.DATE, 1);
                }

                long morningTime = morningDate.getTimeInMillis();
                long afternoonTime = afternoonDate.getTimeInMillis();
                long eveningTime = eveningDate.getTimeInMillis();
                long nightTime = nightDate.getTimeInMillis();

                long alarmTime = Math.min(Math.min(morningTime, afternoonTime), Math.min(eveningTime, nightTime));
                String alarmWallpaper;
                if(alarmTime == morningTime) {
                    alarmWallpaper = MORNING_WALLPAPER_FILENAME;
                } else if(alarmTime == afternoonTime) {
                    alarmWallpaper = AFTERNOON_WALLPAPER_FILENAME;
                } else if(alarmTime == eveningTime) {
                    alarmWallpaper = EVENING_WALLPAPER_FILENAME;
                } else {
                    alarmWallpaper = NIGHT_WALLPAPER_FILENAME;
                }

                Intent intent = new Intent(MainActivity.this, WallpaperBroadcastReceiver.class);
                intent.putExtra(WallpaperBroadcastReceiver.WALLPAPER_FILENAME_KEY, alarmWallpaper);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, 0, pendingIntent);
            } else {
                Intent intent = new Intent(getApplicationContext(), WallpaperBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(AUTO_CHANGE_KEY, isChecked);
            editor.apply();
        }
    };
}