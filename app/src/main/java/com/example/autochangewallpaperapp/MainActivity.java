package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final String TARGET_WALLPAPER = "target_wallpaper";
    private final String MORNING_WALLPAPER = "morning_wallpaper";
    private final String AFTERNOON_WALLPAPER = "afternoon_wallpaper";
    private final String EVENING_WALLPAPER = "evening_wallpaper";
    private final String NIGHT_WALLPAPER = "night_wallpaper";

    private TextView morningWallpaperText;
    private TextView afternoonWallpaperText;
    private TextView eveningWallpaperText;
    private TextView nightWallpaperText;

    private Button morningWallpaperClear;
    private Button afternoonWallpaperClear;
    private Button eveningWallpaperClear;
    private Button nightWallpaperClear;


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

        morningWallpaperClear.setOnClickListener(wallpaperClearListener);
        afternoonWallpaperClear.setOnClickListener(wallpaperClearListener);
        eveningWallpaperClear.setOnClickListener(wallpaperClearListener);
        nightWallpaperClear.setOnClickListener(wallpaperClearListener);

        updateUI();
    }

    private void updateUI() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean morningWallpaperChosen = preferences.contains(MORNING_WALLPAPER);
        boolean afternoonWallpaperChosen = preferences.contains(AFTERNOON_WALLPAPER);
        boolean eveningWallpaperChosen = preferences.contains(EVENING_WALLPAPER);
        boolean nightWallpaperChosen = preferences.contains(NIGHT_WALLPAPER);

        morningWallpaperText.setVisibility(morningWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        afternoonWallpaperText.setVisibility(afternoonWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        eveningWallpaperText.setVisibility(eveningWallpaperChosen ? View.INVISIBLE : View.VISIBLE);
        nightWallpaperText.setVisibility(nightWallpaperChosen ? View.INVISIBLE : View.VISIBLE);

        morningWallpaperClear.setVisibility(morningWallpaperChosen ? View.VISIBLE : View.GONE);
        afternoonWallpaperClear.setVisibility(afternoonWallpaperChosen ? View.VISIBLE : View.GONE);
        eveningWallpaperClear.setVisibility(eveningWallpaperChosen ? View.VISIBLE : View.GONE);
        nightWallpaperClear.setVisibility(nightWallpaperChosen ? View.VISIBLE : View.GONE);
    }

    private final View.OnClickListener wallpaperChooseListener = new View.OnClickListener() {
        public void onClick(View v) {
            final String TAG = "WALLPAPER_CHOOSE_LISTENER";

            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperChoose)) {
                targetWallpaper = MORNING_WALLPAPER;
            } else if(v == findViewById(R.id.afternoonWallpaperChoose)) {
                targetWallpaper = AFTERNOON_WALLPAPER;
            } else if(v == findViewById(R.id.eveningWallpaperChoose)) {
                targetWallpaper = EVENING_WALLPAPER;
            } else if(v == findViewById(R.id.nightWallpaperChoose)) {
                targetWallpaper = NIGHT_WALLPAPER;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TARGET_WALLPAPER, targetWallpaper);
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
                        String targetWallpaper = preferences.getString(TARGET_WALLPAPER, default_str);
                        if (!targetWallpaper.equals(default_str)) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(targetWallpaper, result.toString());
                            editor.apply();
                            updateUI();
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
                targetWallpaper = MORNING_WALLPAPER;
            } else if(v == findViewById(R.id.afternoonWallpaperClear)) {
                targetWallpaper = AFTERNOON_WALLPAPER;
            } else if(v == findViewById(R.id.eveningWallpaperClear)) {
                targetWallpaper = EVENING_WALLPAPER;
            } else if(v == findViewById(R.id.nightWallpaperClear)) {
                targetWallpaper = NIGHT_WALLPAPER;
            } else {
                Log.e(TAG, "Unhandled button click");
                return;
            }

            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.remove(targetWallpaper);
            editor.apply();
            updateUI();
        }
    };
}