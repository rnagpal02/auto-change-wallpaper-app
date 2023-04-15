package com.example.autochangewallpaperapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private OnClickListeners onClickListeners;

    public WallpaperAdapter(OnClickListeners onClickListeners) {
        this.onClickListeners = onClickListeners;
    }

    public interface OnClickListeners {
        void onChooseClick(int position);
        void onClearClick(int position);
        void onPreviewClick(int position);
        void onSetClick(int position);
        void onTimeClick(int position);
    }

    protected class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private final String PREFERENCES_FILENAME = "wallpaper_view_holder";
        private final String PREFERENCES_TARGET_WALLPAPER_KEY = "target_wallpaper";
        private final String PREFERENCES_AUTO_CHANGE_KEY = "auto_change";

        private WallpaperManager wallpaperManager;
        private int position;
        private TextView name;
        private Button choose;
        private Button clear;
        private Button preview;
        private Button set;
        private Button time;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            wallpaperManager = WallpaperManager.getWallpaperManager();
            name = itemView.findViewById(R.id.name);
            choose = itemView.findViewById(R.id.choose);
            clear = itemView.findViewById(R.id.clear);
            preview = itemView.findViewById(R.id.preview);
            set = itemView.findViewById(R.id.set);
            time = itemView.findViewById(R.id.time);
        }

        public void onBindViewHolder(int position) {
            this.position = position;
            choose.setOnClickListener(chooseWallpaperListener);
            clear.setOnClickListener(clearWallpaperListener);
            preview.setOnClickListener(previewWallpaperListener);
            set.setOnClickListener(setWallpaperListener);
            time.setOnClickListener(timeWallpaperListener);
            name.setText("Wallpaper " + position);
        }

        private final View.OnClickListener chooseWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onChooseClick(position);
            }
        };
        private final View.OnClickListener clearWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onClearClick(position);
            }
        };
        private final View.OnClickListener previewWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onPreviewClick(position);
            }
        };
        private final View.OnClickListener setWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onSetClick(position);
            }
        };
        private final View.OnClickListener timeWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onTimeClick(position);
            }
        };
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        holder.onBindViewHolder(position);
    }

    @Override
    public int getItemCount() {
        return WallpaperManager.getWallpaperManager().getNumWallpapers();
    }
}
