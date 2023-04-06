package com.example.autochangewallpaperapp;

public class WallpaperDefaultTimes {
    private static final WallpaperTime DEFAULT_MORNING_TIME = new WallpaperTime(7, 0);
    private static final WallpaperTime DEFAULT_AFTERNOON_TIME = new WallpaperTime(12, 0);
    private static final WallpaperTime DEFAULT_EVENING_TIME = new WallpaperTime(16, 0);
    private static final WallpaperTime DEFAULT_NIGHT_TIME = new WallpaperTime(19, 0);

    public static final WallpaperTime[] DEFAULT_MORNING_AFTERNOON_EVENING_NIGHT_TIMES = {
            DEFAULT_MORNING_TIME, DEFAULT_AFTERNOON_TIME, DEFAULT_EVENING_TIME, DEFAULT_NIGHT_TIME };
}
