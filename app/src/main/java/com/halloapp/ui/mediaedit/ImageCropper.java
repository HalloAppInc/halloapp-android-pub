package com.halloapp.ui.mediaedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCropper {
    @FunctionalInterface
    public interface ImageCroppedListener {
        void cropped(Bitmap originalBitmap, Bitmap croppedBitmap);
    }

    public static void crop(@NonNull Context context, @NonNull File file, @NonNull File target, @NonNull EditImageView.State state, @Nullable ImageCroppedListener listener) {
        BgWorkers.getInstance().execute(() -> {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);

            int limit = Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels));

            final Bitmap bitmap;
            try {
                bitmap = MediaUtils.decodeImage(file, limit, limit);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (bitmap == null) {
                return;
            }

            final float centerX = (float)state.cropWidth / 2;
            final float centerY = (float)state.cropHeight / 2;

            Matrix m = new Matrix();
            m.postTranslate(centerX - ((float)bitmap.getWidth() / 2), centerY - ((float)bitmap.getHeight() / 2));

            m.postRotate(-90 * state.numberOfRotations, centerX, centerY);
            m.postScale(state.vFlipped ? -state.scale : state.scale, state.hFlipped ? -state.scale : state.scale, centerX, centerY);
            m.postTranslate(-state.cropOffsetX, -state.cropOffsetY);

            Bitmap cropped = Bitmap.createBitmap(state.cropWidth, state.cropHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(cropped);
            canvas.drawBitmap(bitmap, m, null);

            try (final FileOutputStream stream = new FileOutputStream(target)) {
                cropped.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, stream);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            if (listener != null) {
                new Handler(context.getMainLooper()).post(() -> listener.cropped(bitmap, cropped));
            }
        });
    }
}
