package com.example.autochangewallpaperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.Shapeable;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
    private Context context;
    private OnClickListeners onClickListeners;

    public WallpaperAdapter(Context context, OnClickListeners onClickListeners) {
        this.context = context;
        this.onClickListeners = onClickListeners;
    }

    public interface OnClickListeners {
        void onChooseClick(int position);
        void onClearClick(int position);
        void onSetClick(int position);
        void onTimeClick(int position);
    }

    protected class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private final double PREVIEW_SCALE = 0.5;

        private WallpaperManager wallpaperManager;
        private int position;
        private TextView name;
        private ShapeableImageView preview;
        private Button choose;
        private Button clear;
        private Button set;
        private Button time;

        public WallpaperViewHolder(View itemView) {
            super(itemView);

            wallpaperManager = WallpaperManager.getWallpaperManager();
            name = itemView.findViewById(R.id.name);
            preview = itemView.findViewById(R.id.preview);
            choose = itemView.findViewById(R.id.choose);
            clear = itemView.findViewById(R.id.clear);
            set = itemView.findViewById(R.id.set);
            time = itemView.findViewById(R.id.time);
        }

        public void onBindViewHolder(int position) {
            this.position = position;

            Bitmap bitmap = wallpaperManager.getBitmap(context, position);
            if(bitmap.getWidth() > bitmap.getHeight()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            // Set width and height of imageview
            int imageWidth = (int)(context.getResources().getDisplayMetrics().widthPixels * PREVIEW_SCALE);
            int imageHeight = (int)(context.getResources().getDisplayMetrics().heightPixels * PREVIEW_SCALE);
            preview.getLayoutParams().width = imageWidth;
            preview.getLayoutParams().height = imageHeight;

            // Scale image so one dimension fits perfectly in view, and second dimension is at least as big as view
            double widthScale = (double)(imageWidth) / (double)(bitmap.getWidth());
            double heightScale = (double)imageHeight / (double)bitmap.getHeight();
            double scale = Double.max(widthScale, heightScale);
            preview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * scale), (int)(bitmap.getHeight() * scale), true));

            choose.setOnClickListener(chooseWallpaperListener);
            clear.setOnClickListener(clearWallpaperListener);
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
