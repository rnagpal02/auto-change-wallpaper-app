package com.example.autochangewallpaperapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {

    protected class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private Button choose;
        private Button clear;
        private Button preview;
        private Button time;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            choose = itemView.findViewById(R.id.choose);
            clear = itemView.findViewById(R.id.clear);
            preview = itemView.findViewById(R.id.preview);
            time = itemView.findViewById(R.id.time);
        }

        public void onBindViewHolder(int position) {
            name.setText("Wallpaper " + position);
        }
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
        return 10;
    }
}
