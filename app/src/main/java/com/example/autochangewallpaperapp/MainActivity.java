package com.example.autochangewallpaperapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class MainActivity extends AppCompatActivity implements WallpaperAdapter.OnClickListeners {
    private final String PREFERENCES_FIRST_RUN_KEY = "first_run";
    private final String PREFERENCES_TARGET_WALLPAPER_KEY = "target_wallpaper";
    private final String PREFERENCES_AUTO_CHANGE_KEY = "auto_change";

    com.example.autochangewallpaperapp.WallpaperManager wallpaperManager; // TODO change class name
    private MaterialToolbar toolbar;
    private RecyclerView wallpaperRecycler;
    private WallpaperAdapter adapter;
    private MaterialSwitch autoChangeWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instance of singleton class
        wallpaperManager = WallpaperManager.getWallpaperManager();

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(navigationClickListener);
        toolbar.setOnMenuItemClickListener(menuItemClickListener);

        // Setup recycler view
        wallpaperRecycler = findViewById(R.id.wallpaperRecycler);
        wallpaperRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new WallpaperAdapter(this, this);
        wallpaperRecycler.setAdapter(adapter);

        // Scroll one item at a time
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(wallpaperRecycler);

        // Add padding to center first and last item
        wallpaperRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int itemOffset = (wallpaperManager.getDisplayWidth() - adapter.getItemWidth()) / 2;
                int itemPosition = parent.getChildAdapterPosition(view);
                if(itemPosition == 0) {
                    outRect.left = itemOffset;
                } else if(itemPosition == state.getItemCount() - 1) {
                    outRect.right = itemOffset;
                }
            }
        });

        // Setup auto changing toggle
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

            wallpaperManager.createDefaults(MainActivity.this, WallpaperDefaultProperties.DEFAULT_MORNING_AFTERNOON_EVENING_NIGHT_PROPERTIES);

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

    private View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO implement side bar for navigation
        }
    };

    private Toolbar.OnMenuItemClickListener menuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final String TAG = "MENU_CLICK_LISTENER";
            switch (item.getItemId()) {
                case R.id.edit:
                    // TODO implement editing mode
                    break;
                default:
                    Log.e(TAG, "Unhandled menu item clicked");
                    break;
            }
            return false;
        }
    };

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

        boolean isOk = wallpaperManager.uploadWallpaper(MainActivity.this, targetWallpaper, uri);
        if(!isOk) {
            Toast.makeText(MainActivity.this, "Error downloading wallpaper", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.notifyItemChanged(targetWallpaper);
        updateUI();
    });

    @Override
    public void onDownloadClick(int position) {
        boolean isOk = wallpaperManager.downloadWallpaper(position);
        if(!isOk) {
            Toast.makeText(MainActivity.this, "Unable to download wallpaper", Toast.LENGTH_SHORT).show();
        }
    }

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
        // Get wallpaper's current time
        WallpaperTime currentTime = wallpaperManager.getTime(position);

        // Setup properties of timepicker
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
        builder.setHour(currentTime.hour);
        builder.setMinute(currentTime.minute);
        builder.setTimeFormat(TimeFormat.CLOCK_12H);
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);

        // Show time picker dialog
        MaterialTimePicker timePicker = builder.build();
        timePicker.show(getSupportFragmentManager(), "TimePicker");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallpaperTime time = new WallpaperTime(timePicker.getHour(), timePicker.getMinute());
                wallpaperManager.setTime(MainActivity.this, position, time);
                adapter.notifyItemChanged(position);
            }
        });
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