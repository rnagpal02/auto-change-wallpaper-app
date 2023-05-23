package com.example.autochangewallpaperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private final Context context;
    private final OnClickListeners onClickListeners;

    public WallpaperAdapter(Context context, OnClickListeners onClickListeners) {
        this.context = context;
        this.onClickListeners = onClickListeners;
    }

    public interface OnClickListeners {
        void onChooseClick(int position);
        void onDownloadClick(int position);
        void onClearClick(int position);
        void onSetClick(int position);
        void onTimeClick(int position);
    }

    protected class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private final WallpaperManager wallpaperManager;
        private int position;
        private final TextView name;
        private final ShapeableImageView preview;
        private final Button choose;
        private final Button download;
        private final Button clear;
        private final Button set;
        private final Button time;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            wallpaperManager = WallpaperManager.getWallpaperManager();
            name = itemView.findViewById(R.id.name);
            preview = itemView.findViewById(R.id.preview);
            choose = itemView.findViewById(R.id.choose);
            download = itemView.findViewById(R.id.download);
            clear = itemView.findViewById(R.id.clear);
            set = itemView.findViewById(R.id.set);
            time = itemView.findViewById(R.id.time);
        }

        public void onBindViewHolder(int position) {
            this.position = position;

            // Set preview from bitmap
            Bitmap bitmap = wallpaperManager.getBitmap(position);
            if(bitmap == null) {
                bitmap = Bitmap.createBitmap(wallpaperManager.getDisplayWidth(), wallpaperManager.getDisplayHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT);
            }
            preview.setImageBitmap(bitmap);

            choose.setOnClickListener(chooseWallpaperListener);
            download.setOnClickListener(downloadWallpaperListener);
            clear.setOnClickListener(clearWallpaperListener);
            set.setOnClickListener(setWallpaperListener);

            // Set text of time button to be wallpaper's time
            WallpaperTime wallpaperTime = wallpaperManager.getTime(position);
            boolean isAM = wallpaperTime.hour < 12;
            int hour = wallpaperTime.hour % 12;
            if(hour == 0) {
                hour = 12;
            }
            int minute = wallpaperTime.minute;
            String timeText = hour + ":" + String.format("%02d", minute) + " " + (isAM ? "AM" : "PM");
            time.setText(timeText);
            time.setOnClickListener(timeWallpaperListener);

            name.setText("Wallpaper " + position);
        }

        private final View.OnClickListener chooseWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onChooseClick(position);
            }
        };
        private final View.OnClickListener downloadWallpaperListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListeners.onDownloadClick(position);
            }
        };
        private final View.OnClickListener clearWallpaperListener = new View.OnClickListener() {
            public void onClick(View v) {
                onClickListeners.onClearClick(position);
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
