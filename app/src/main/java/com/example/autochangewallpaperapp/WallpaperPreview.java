package com.example.autochangewallpaperapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;
import java.net.URI;

public class WallpaperPreview extends AppCompatActivity {
    public static final String WALLPAPER_FILENAME_KEY = "wallpaper_filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);

        ImageView wallpaper = findViewById(R.id.wallpaper);
        String filename = getIntent().getStringExtra(WALLPAPER_FILENAME_KEY);
        File file = getFileStreamPath(filename);
        Uri uri = Uri.fromFile(file);
        wallpaper.setImageURI(uri);
    }
}