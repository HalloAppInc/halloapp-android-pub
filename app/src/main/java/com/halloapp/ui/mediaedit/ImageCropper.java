package com.halloapp.ui.mediaedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCropper {

    @WorkerThread
    public static void crop(@NonNull Context context, @NonNull File file, @NonNull File target, @NonNull EditImageView.State state) {
        final Bitmap bitmap;
        try {
            bitmap = MediaUtils.decodeImage(context, file);
        } catch (IOException e) {
            Log.w("ImageCropper: unable to decode file", e);
            return;
        }

        if (bitmap == null) {
            Log.w("ImageCropper: unable to load file");
            return;
        }

        final float centerX = (float)state.cropWidth / 2;
        final float centerY = (float)state.cropHeight / 2;

        Matrix m = new Matrix();
        m.postTranslate(centerX - ((float)bitmap.getWidth() / 2), centerY - ((float)bitmap.getHeight() / 2));

        m.postRotate(-90 * state.rotationCount, centerX, centerY);
        m.postScale(state.vFlipped ? -state.scale : state.scale, state.hFlipped ? -state.scale : state.scale, centerX, centerY);
        m.postTranslate(-state.cropOffsetX, -state.cropOffsetY);

        Bitmap cropped = Bitmap.createBitmap(state.cropWidth, state.cropHeight, MediaUtils.getBitmapConfig(bitmap.getConfig()));
        Canvas canvas = new Canvas(cropped);
        canvas.drawBitmap(bitmap, m, null);

        try (final FileOutputStream stream = new FileOutputStream(target)) {
            cropped.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, stream);
        } catch (IOException e) {
            Log.e("ImageCropper: unable to save and compress cropped image", e);
        }
    }
}
