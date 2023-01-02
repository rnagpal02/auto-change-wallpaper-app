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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private final String TARGET_WALLPAPER = "target_wallpaper";
    private final String MORNING_WALLPAPER = "morning_wallpaper";
    private final String AFTERNOON_WALLPAPER = "afternoon_wallpaper";
    private final String EVENING_WALLPAPER = "evening_wallpaper";
    private final String NIGHT_WALLPAPER = "night_wallpaper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.morningWallpaperButton).setOnClickListener(wallpaperButtonListener);
        findViewById(R.id.afternoonWallpaperButton).setOnClickListener(wallpaperButtonListener);
        findViewById(R.id.eveningWallpaperButton).setOnClickListener(wallpaperButtonListener);
        findViewById(R.id.nightWallpaperButton).setOnClickListener(wallpaperButtonListener);
    }

    private final View.OnClickListener wallpaperButtonListener = new View.OnClickListener() {
        final String TAG = "WALLPAPER_BUTTON_LISTENER";
        public void onClick(View v) {
            String targetWallpaper;
            if(v == findViewById(R.id.morningWallpaperButton)) {
                targetWallpaper = MORNING_WALLPAPER;
            } else if(v == findViewById(R.id.afternoonWallpaperButton)) {
                targetWallpaper = AFTERNOON_WALLPAPER;
            } else if(v == findViewById(R.id.eveningWallpaperButton)) {
                targetWallpaper = EVENING_WALLPAPER;
            } else if(v == findViewById(R.id.nightWallpaperButton)) {
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
                            editor.putString(TARGET_WALLPAPER, result.toString());
                        } else {
                            Log.e(TAG, "Target wallpaper not found");
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No Wallpaper Chosen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}