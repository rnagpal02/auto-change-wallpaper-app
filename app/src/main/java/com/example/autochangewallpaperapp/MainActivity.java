package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity implements WallpaperAdapter.OnClickListeners {
    private final String PREFERENCES_FIRST_RUN_KEY = "first_run";
    private final String PREFERENCES_TARGET_WALLPAPER_KEY = "target_wallpaper";
    private final String PREFERENCES_AUTO_CHANGE_KEY = "auto_change";

    com.example.autochangewallpaperapp.WallpaperManager wallpaperManager; // TODO change class name
    private RecyclerView wallpaperRecycler;
    private WallpaperAdapter adapter;
    private MaterialSwitch autoChangeWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wallpaperManager = com.example.autochangewallpaperapp.WallpaperManager.getWallpaperManager();

        wallpaperRecycler = findViewById(R.id.wallpaperRecycler);
        wallpaperRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new WallpaperAdapter(this, this);
        wallpaperRecycler.setAdapter(adapter);

        autoChangeWallpaper = findViewById(R.id.autoChangeSwitch);

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
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean default_value = false;
        autoChangeWallpaper.setChecked(preferences.getBoolean(PREFERENCES_AUTO_CHANGE_KEY, default_value));
    }

    @Override
    public void onChooseClick(int position) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFERENCES_TARGET_WALLPAPER_KEY, position);
        editor.apply();

        chooseWallpaper.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    ActivityResultLauncher<PickVisualMediaRequest> chooseWallpaper = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        final String TAG = "CHOOSE_WALLPAPER_ACTIVITY_RESULT";

        int targetWallpaper;
        int default_value = -1;
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        targetWallpaper = preferences.getInt(PREFERENCES_TARGET_WALLPAPER_KEY, default_value);
        if(targetWallpaper == default_value) {
            Log.e(TAG, "Target wallpaper not found");
            return;
        }

        boolean isOk = wallpaperManager.downloadWallpaper(MainActivity.this, targetWallpaper, uri);
        if(!isOk) {
            Toast.makeText(MainActivity.this, "Error downloading wallpaper", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.notifyItemChanged(targetWallpaper);
        updateUI();
    });

    @Override
    public void onClearClick(int position) {
        wallpaperManager.clearWallpaper(MainActivity.this, position);
        adapter.notifyItemChanged(position);
        updateUI();
    }

    @Override
    public void onSetClick(int position) {
        boolean isOk = wallpaperManager.setWallpaper(MainActivity.this, position);
        if(!isOk){
            Toast.makeText(MainActivity.this, "Unable to set wallpaper", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTimeClick(int position) {
        WallpaperTime currentTime = wallpaperManager.getTime(position);
        int finalTargetWallpaper = position;
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                WallpaperTime time = new WallpaperTime(hour, minute);
                wallpaperManager.setTime(MainActivity.this, finalTargetWallpaper, time);
            }
        }, currentTime.hour, currentTime.minute, false);
        timePickerDialog.show();
    }

    private final CompoundButton.OnCheckedChangeListener autoChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked) {
                boolean isOk = wallpaperManager.startAutoChange(MainActivity.this);
                if(!isOk) {
                    Toast.makeText(getApplicationContext(), "Finish choosing all wallpapers", Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(false);
                    return;
                }
            } else {
                wallpaperManager.stopAutoChange(MainActivity.this);
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREFERENCES_AUTO_CHANGE_KEY, isChecked);
            editor.apply();
        }
    };
}