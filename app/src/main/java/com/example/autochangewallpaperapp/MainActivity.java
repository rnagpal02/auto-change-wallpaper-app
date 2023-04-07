package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final String PREFERENCES_FIRST_RUN_KEY = "first_run";
    private final String PREFERENCES_TARGET_WALLPAPER_KEY = "target_wallpaper";
    private final String PREFERENCES_AUTO_CHANGE_KEY = "auto_change";

    private final int NUM_WALLPAPERS = 4;

    com.example.autochangewallpaperapp.WallpaperManager wallpaperManager; // TODO change class name
    WallpaperUI[] wallpaperUIs;
    private Switch autoChangeWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wallpaperManager = com.example.autochangewallpaperapp.WallpaperManager.getWallpaperManager();
        wallpaperUIs = new WallpaperUI[NUM_WALLPAPERS];
        for(int i = 0; i < NUM_WALLPAPERS; ++i) {
            wallpaperUIs[i] = new WallpaperUI();
        }

        wallpaperUIs[0].chooseWallpaperText = findViewById(R.id.morningWallpaperText);
        wallpaperUIs[1].chooseWallpaperText = findViewById(R.id.afternoonWallpaperText);
        wallpaperUIs[2].chooseWallpaperText = findViewById(R.id.eveningWallpaperText);
        wallpaperUIs[3].chooseWallpaperText = findViewById(R.id.nightWallpaperText);

        wallpaperUIs[0].chooseWallpaperButton = findViewById(R.id.morningWallpaperChoose);
        wallpaperUIs[1].chooseWallpaperButton = findViewById(R.id.afternoonWallpaperChoose);
        wallpaperUIs[2].chooseWallpaperButton = findViewById(R.id.eveningWallpaperChoose);
        wallpaperUIs[3].chooseWallpaperButton = findViewById(R.id.nightWallpaperChoose);

        wallpaperUIs[0].clearWallpaperButton = findViewById(R.id.morningWallpaperClear);
        wallpaperUIs[1].clearWallpaperButton = findViewById(R.id.afternoonWallpaperClear);
        wallpaperUIs[2].clearWallpaperButton = findViewById(R.id.eveningWallpaperClear);
        wallpaperUIs[3].clearWallpaperButton = findViewById(R.id.nightWallpaperClear);

        wallpaperUIs[0].previewWallpaperButton = findViewById(R.id.morningWallpaperPreview);
        wallpaperUIs[1].previewWallpaperButton = findViewById(R.id.afternoonWallpaperPreview);
        wallpaperUIs[2].previewWallpaperButton = findViewById(R.id.eveningWallpaperPreview);
        wallpaperUIs[3].previewWallpaperButton = findViewById(R.id.nightWallpaperPreview);

        wallpaperUIs[0].setWallpaperButton = findViewById(R.id.morningWallpaperSet);
        wallpaperUIs[1].setWallpaperButton = findViewById(R.id.afternoonWallpaperSet);
        wallpaperUIs[2].setWallpaperButton = findViewById(R.id.eveningWallpaperSet);
        wallpaperUIs[3].setWallpaperButton = findViewById(R.id.nightWallpaperSet);

        wallpaperUIs[0].setTimeButton = findViewById(R.id.morningWallpaperTime);
        wallpaperUIs[1].setTimeButton = findViewById(R.id.afternoonWallpaperTime);
        wallpaperUIs[2].setTimeButton = findViewById(R.id.eveningWallpaperTime);
        wallpaperUIs[3].setTimeButton = findViewById(R.id.nightWallpaperTime);

        autoChangeWallpaper = findViewById(R.id.autoChangeSwitch);

        for(WallpaperUI ui : wallpaperUIs) {
            ui.setListeners();
        }
        autoChangeWallpaper.setOnCheckedChangeListener(autoChangeListener);

        checkFirstRun();
        updateUI();
    }

    private void checkFirstRun() {
        final String TAG = "CHECK_FIRST_RUN";

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean is_first_run = preferences.getBoolean(PREFERENCES_FIRST_RUN_KEY, true);
        if(is_first_run) {
            Log.d(TAG, "First run");

            wallpaperManager.createDefaults(MainActivity.this, WallpaperDefaultTimes.DEFAULT_MORNING_AFTERNOON_EVENING_NIGHT_TIMES);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREFERENCES_AUTO_CHANGE_KEY, false);
            editor.putBoolean(PREFERENCES_FIRST_RUN_KEY, false);
            editor.apply();
            Log.d(TAG, "Set default wallpaper times");
        } else {
            wallpaperManager.initWallpapers(MainActivity.this);
        }
    }

    private void updateUI() {
        for(int i = 0; i < NUM_WALLPAPERS; ++i) {
            boolean wallpaperChosen = wallpaperManager.isWallpaperChosen(MainActivity.this, i);
            wallpaperUIs[i].setVisibilities(wallpaperChosen);
            wallpaperUIs[i].setClickabilities(wallpaperChosen);
        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean default_value = false;
        autoChangeWallpaper.setChecked(preferences.getBoolean(PREFERENCES_AUTO_CHANGE_KEY, default_value));
    }

    private final View.OnClickListener chooseWallpaperListener = new View.OnClickListener() {
        public void onClick(View v) {
            final String TAG = "WALLPAPER_CHOOSE_LISTENER";

            int targetWallpaper = -1;
            for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                if(v == wallpaperUIs[i].chooseWallpaperButton) {
                    targetWallpaper = i;
                    break;
                }
            }
            if(targetWallpaper < 0) {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREFERENCES_TARGET_WALLPAPER_KEY, targetWallpaper);
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

                    int targetWallpaper;
                    int default_value = -1;
                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                    targetWallpaper = preferences.getInt(PREFERENCES_TARGET_WALLPAPER_KEY, default_value);
                    if(targetWallpaper == default_value) {
                        Log.e(TAG, "Target wallpaper not found");
                        return;
                    }

                    boolean isOk = wallpaperManager.downloadWallpaper(MainActivity.this, targetWallpaper, result);
                    if(!isOk) {
                        Toast.makeText(MainActivity.this, "Error downloading wallpaper", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateUI();
                }
            });

    private final View.OnClickListener clearWallpaperListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_CLEAR_LISTENER";

            int targetWallpaper = -1;
            for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                if(v == wallpaperUIs[i].clearWallpaperButton) {
                    targetWallpaper = i;
                    break;
                }
            }
            if(targetWallpaper < 0) {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            wallpaperManager.clearWallpaper(MainActivity.this, targetWallpaper);
            updateUI();
        }
    };

    private final View.OnClickListener previewWallpaperListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_PREVIEW_LISTENER";

            int targetWallpaper = -1;
            for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                if(v == wallpaperUIs[i].previewWallpaperButton) {
                    targetWallpaper = i;
                    break;
                }
            }
            if(targetWallpaper < 0) {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            String filename = wallpaperManager.getFilename(targetWallpaper);
            Intent intent = new Intent(getApplicationContext(), WallpaperPreview.class);
            intent.putExtra(WallpaperPreview.EXTRA_WALLPAPER_FILENAME_KEY, filename);
            startActivity(intent);
        }
    };

    private final View.OnClickListener setWallpaperListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String TAG = "WALLPAPER_SET_LISTENER";

            int targetWallpaper = -1;
            for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                if(v == wallpaperUIs[i].setWallpaperButton) {
                    targetWallpaper = i;
                    break;
                }
            }
            if(targetWallpaper < 0) {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            boolean isOk = wallpaperManager.setWallpaper(MainActivity.this, targetWallpaper);
            if(!isOk){
                Toast.makeText(MainActivity.this, "Unable to set wallpaper", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final View.OnClickListener setTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String TAG = "WALLPAPER_TIME_LISTENER";

            int targetWallpaper = -1;
            for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                if(view == wallpaperUIs[i].setTimeButton) {
                    targetWallpaper = i;
                    break;
                }
            }
            if(targetWallpaper < 0) {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            WallpaperTime currentTime = wallpaperManager.getTime(targetWallpaper);
            int finalTargetWallpaper = targetWallpaper;
            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    WallpaperTime time = new WallpaperTime(hour, minute);
                    wallpaperManager.setTime(MainActivity.this, finalTargetWallpaper, time);
                }
            }, currentTime.hour, currentTime.minute, false);
            timePickerDialog.show();
        }
    };

    private final CompoundButton.OnCheckedChangeListener autoChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked) {
                for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                    boolean wallpaperChosen = wallpaperManager.isWallpaperChosen(MainActivity.this, i);
                    if(!wallpaperChosen) {
                        Toast.makeText(getApplicationContext(), "Finish choosing all wallpapers", Toast.LENGTH_SHORT).show();
                        compoundButton.setChecked(false);
                        return;
                    }
                }

                Calendar targetDate = Calendar.getInstance();
                int currentMinutes = targetDate.get(Calendar.HOUR_OF_DAY) * 60 + targetDate.get(Calendar.MINUTE);
                int targetWallpaper = -1;
                WallpaperTime targetTime = wallpaperManager.getTime(0);
                for(int i = 0; i < NUM_WALLPAPERS; ++i) {
                    WallpaperTime wallpaperTime = wallpaperManager.getTime(i);
                    int wallpaperMinutes = wallpaperTime.getTimeMinutes();
                    if(wallpaperMinutes > currentMinutes) {
                        targetWallpaper = i;
                        targetTime = wallpaperTime;
                        break;
                    }
                }

                if(targetWallpaper < 0) {
                    targetDate.add(Calendar.DATE, 1);
                    targetWallpaper = 0;
                }
                targetDate.set(Calendar.HOUR_OF_DAY, targetTime.hour);
                targetDate.set(Calendar.MINUTE, targetTime.minute);
                targetDate.set(Calendar.SECOND, 0);
                targetDate.set(Calendar.MILLISECOND, 0);

                long targetTimeMillis = targetDate.getTimeInMillis();
                Intent intent = getBroadcastIntent();
                intent.putExtra(WallpaperManager.EXTRA_TARGET_WALLPAPER_KEY, targetWallpaper);
                PendingIntent pendingIntent = getBroadcastPendingIntent(intent);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, targetTimeMillis, pendingIntent);
            } else {
                Intent intent = getBroadcastIntent();
                PendingIntent pendingIntent = getBroadcastPendingIntent(intent);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREFERENCES_AUTO_CHANGE_KEY, isChecked);
            editor.apply();
        }
    };

    private Intent getBroadcastIntent() {
        return new Intent(MainActivity.this, WallpaperBroadcastReceiver.class);
    }

    private PendingIntent getBroadcastPendingIntent(Intent intent) {
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private class WallpaperUI {
        public TextView chooseWallpaperText;
        public Button chooseWallpaperButton;
        public Button clearWallpaperButton;
        public Button previewWallpaperButton;
        public Button setWallpaperButton;
        public Button setTimeButton;

        public void setListeners() {
            chooseWallpaperButton.setOnClickListener(chooseWallpaperListener);
            clearWallpaperButton.setOnClickListener(clearWallpaperListener);
            previewWallpaperButton.setOnClickListener(previewWallpaperListener);
            setWallpaperButton.setOnClickListener(setWallpaperListener);
            setTimeButton.setOnClickListener(setTimeListener);
        }

        public void setVisibilities(boolean wallpaperChosen) {
            chooseWallpaperText.setVisibility(wallpaperChosen ? View.INVISIBLE : View.VISIBLE);
            clearWallpaperButton.setVisibility(wallpaperChosen ? View.VISIBLE : View.GONE);
        }

        public void setClickabilities(boolean wallpaperChosen) {
            previewWallpaperButton.setClickable(wallpaperChosen);
            setWallpaperButton.setClickable(wallpaperChosen);
        }
    }
}