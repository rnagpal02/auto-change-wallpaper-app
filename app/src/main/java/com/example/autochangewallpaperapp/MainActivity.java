package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private final String TARGET_WALLPAPER_FILENAME = "target_wallpaper";
    private final String MORNING_WALLPAPER_FILENAME = "morning_wallpaper";
    private final String AFTERNOON_WALLPAPER_FILENAME = "afternoon_wallpaper";
    private final String EVENING_WALLPAPER_FILENAME = "evening_wallpaper";
    private final String NIGHT_WALLPAPER_FILENAME = "night_wallpaper";

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

        updateUI();
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
}