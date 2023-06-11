package com.example.autochangewallpaperapp;

public class WallpaperProperties {
    private String name;
    private WallpaperTime time;

    WallpaperProperties(String name, WallpaperTime time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WallpaperTime getTime() {
        return time;
    }

    public void setTime(WallpaperTime time) {
        this.time = time;
    }
}
