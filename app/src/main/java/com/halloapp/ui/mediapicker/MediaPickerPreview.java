package com.halloapp.ui.mediapicker;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.halloapp.R;
import com.halloapp.util.Log;

import java.io.IOException;

public class MediaPickerPreview {
    private final Context context;
    private PopupWindow popup;
    private final ViewOutlineProvider vop = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            int radius = context.getResources().getDimensionPixelSize(R.dimen.media_gallery_preview_radius);
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
        }
    };

    public MediaPickerPreview(Context context) {
        this.context = context;
    }

    private void dimBackground() {
        final View rootView = popup.getContentView().getRootView();
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) rootView.getLayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        wm.updateViewLayout(rootView, layoutParams);
    }

    private void setVideo(TextureView view, Uri uri) {
        final MediaPlayer player = new MediaPlayer();

        view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int surfaceWidth, int surfaceHeight) {
                player.setOnVideoSizeChangedListener((mediaPlayer, width, height) -> {
                    final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(displayMetrics);

                    int padding = context.getResources().getDimensionPixelSize(R.dimen.media_gallery_preview_padding);
                    float scale = Math.min((displayMetrics.widthPixels - padding) / (float) width, (displayMetrics.heightPixels - padding) / (float) height);

                    view.setLayoutParams(new ConstraintLayout.LayoutParams(Math.round(scale * (float) width), Math.round(scale * (float) height)));
                });

                player.setSurface(new Surface(surfaceTexture));

                try {
                    player.setDataSource(context, uri);
                    player.prepare();
                } catch (IOException e) {
                    hide();
                    return;
                }

                player.setLooping(true);
                player.start();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                player.stop();
                player.release();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
    }

    public void show(@NonNull GalleryItem galleryItem, View anchor) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id);

        View content = LayoutInflater.from(context).inflate(R.layout.media_popup_preview, null);
        ImageView iv = content.findViewById(R.id.image);
        TextureView tv = content.findViewById(R.id.video);

        iv.setOutlineProvider(vop);
        iv.setClipToOutline(true);
        tv.setOutlineProvider(vop);
        tv.setClipToOutline(true);

        if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            tv.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
            iv.setImageURI(uri);
        } else if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            tv.setVisibility(View.VISIBLE);
            iv.setVisibility(View.GONE);
            setVideo(tv, uri);
        }

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        popup = new PopupWindow(content, width, height, true);
        popup.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        popup.setAnimationStyle(R.style.MediaPreviewAnimation);
        popup.showAtLocation(anchor, Gravity.CENTER, 0, 0);
        popup.setOnDismissListener(() -> popup = null);

        dimBackground();
    }

    public void hide() {
        popup.dismiss();
    }

    public boolean isShowing() {
        return popup != null && popup.isShowing();
    }
}
