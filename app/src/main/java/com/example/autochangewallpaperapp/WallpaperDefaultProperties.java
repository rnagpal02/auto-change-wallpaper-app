package com.example.autochangewallpaperapp;

public class WallpaperDefaultProperties {
    private static final String MORNING_NAME = "Morning";
    private static final String AFTERNOON_NAME = "Afternoon";
    private static final String EVENING_NAME = "Evening";
    private static final String NIGHT_NAME = "Night";

    private static final WallpaperTime MORNING_TIME = new WallpaperTime(7,0);
    private static final WallpaperTime AFTERNOON_TIME = new WallpaperTime(12,0);
    private static final WallpaperTime EVENING_TIME = new WallpaperTime(16,0);
    private static final WallpaperTime NIGHT_TIME = new WallpaperTime(19,0);

    private static final WallpaperProperties MORNING_PROPERTY = new WallpaperProperties(MORNING_NAME, MORNING_TIME);
    private static final WallpaperProperties AFTERNOON_PROPERTY = new WallpaperProperties(AFTERNOON_NAME, AFTERNOON_TIME);
    private static final WallpaperProperties EVENING_PROPERTY = new WallpaperProperties(EVENING_NAME, EVENING_TIME);
    private static final WallpaperProperties NIGHT_PROPERTY = new WallpaperProperties(NIGHT_NAME, NIGHT_TIME);

    public static final WallpaperProperties[] DEFAULT_MORNING_AFTERNOON_EVENING_NIGHT_PROPERTIES = {
            MORNING_PROPERTY, AFTERNOON_PROPERTY, EVENING_PROPERTY, NIGHT_PROPERTY };
}
