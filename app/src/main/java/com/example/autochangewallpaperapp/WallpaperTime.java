package com.example.autochangewallpaperapp;

public class WallpaperTime {
    public int hour;
    public int minute;

    public WallpaperTime(int hour, int minute) {
        setTime(hour, minute);
    }

    public WallpaperTime(int minutes) {
        setTime(minutes);
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public void setTime(int minutes) {
        this.hour = minutes / 60;
        this.minute = minutes % 60;
    }

    public int getTimeMinutes() {
        return hour * 60 + minute;
    }
}
