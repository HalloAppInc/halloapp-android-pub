package com.halloapp.ui.mediapicker;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.exifinterface.media.ExifInterface;

import com.halloapp.R;
import com.halloapp.media.MediaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

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
            new ImageLoaderTask(iv, uri).execute();
        } else if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            tv.setVisibility(View.VISIBLE);
            iv.setVisibility(View.GONE);
            new VideoLoaderTask(tv, uri).execute();
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

    private static class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<ImageView> viewRef;
        private Uri uri;

        ImageLoaderTask(ImageView view, Uri uri) {
            viewRef = new WeakReference<>(view);
            this.uri = uri;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            ImageView view = viewRef.get();

            if (view != null) {
                try {
                    InputStream inputStream = view.getContext().getContentResolver().openInputStream(uri);
                    if (inputStream == null) {
                        return null;
                    }

                    ExifInterface exif = new ExifInterface(inputStream);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1); // to get orientation (e.g. ORIENTATION_ROTATE_90, ORIENTATION_ROTATE_180) –
                    inputStream.close();

                    final Matrix matrix = MediaUtils.fromOrientation(orientation);
                    Bitmap bitmap = BitmapFactory.decodeStream(view.getContext().getContentResolver().openInputStream(uri));

                    final Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    if (result != bitmap) {
                        bitmap.recycle();
                    }

                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView view = viewRef.get();

            if (bitmap != null && view != null) {
                view.setImageBitmap(bitmap);
            }
        }
    }

    private static class VideoLoaderTask extends AsyncTask<Void, Void, MediaPlayer> {
        private WeakReference<TextureView> viewRef;
        private Uri uri;

        VideoLoaderTask(TextureView view, Uri uri) {
            viewRef = new WeakReference<>(view);
            this.uri = uri;
        }

        @Override
        protected MediaPlayer doInBackground(Void... voids) {
            final MediaPlayer player = new MediaPlayer();
            final TextureView view = viewRef.get();

            if (view != null) {
                try {
                    player.setDataSource(view.getContext(), uri);
                } catch (IOException e) {
                    return null;
                }

                player.setOnVideoSizeChangedListener((mediaPlayer, width, height) -> {
                    final WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(displayMetrics);

                    int padding = view.getContext().getResources().getDimensionPixelSize(R.dimen.media_gallery_preview_padding);
                    float scale = Math.min((displayMetrics.widthPixels - padding) / (float) width, (displayMetrics.heightPixels - padding) / (float) height);

                    view.setLayoutParams(new ConstraintLayout.LayoutParams(Math.round(scale * (float) width), Math.round(scale * (float) height)));
                });
            }

            return player;
        }

        private void attachSurface(MediaPlayer player, SurfaceTexture surfaceTexture) {
            player.setSurface(new Surface(surfaceTexture));

            try {
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.setLooping(true);
            player.start();
        }

        @Override
        protected void onPostExecute(MediaPlayer player) {
            TextureView view = viewRef.get();

            if (view != null) {
                view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int surfaceWidth, int surfaceHeight) {
                        attachSurface(player, surfaceTexture);
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

                if (view.isAvailable()) {
                    attachSurface(player, view.getSurfaceTexture());
                }
            }
        }
    }
}
