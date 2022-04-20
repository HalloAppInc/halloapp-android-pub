package com.halloapp.ui.mediaedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

        float scaledCropWidth = (float) state.cropWidth / state.scale;
        float scaledCropHeight = (float) state.cropHeight / state.scale;
        float cropCenterX = scaledCropWidth / 2;
        float cropCenterY = scaledCropHeight / 2;

        Matrix m = new Matrix();
        m.postTranslate(cropCenterX - ((float)bitmap.getWidth() / 2), cropCenterY - ((float)bitmap.getHeight() / 2));
        m.postRotate(-90 * state.rotationCount, cropCenterX, cropCenterY);
        m.postScale(state.vFlipped ? -1 : 1, state.hFlipped ? -1 : 1, cropCenterX, cropCenterY);
        m.postTranslate(-state.cropOffsetX / state.scale, -state.cropOffsetY / state.scale);

        Bitmap cropped = Bitmap.createBitmap((int)scaledCropWidth, (int)scaledCropHeight, MediaUtils.getBitmapConfig(bitmap.getConfig()));
        Canvas canvas = new Canvas(cropped);
        canvas.drawBitmap(bitmap, m, null);

        canvas.save();

        if (state.rotationCount % 2 == 0) {
            canvas.translate(cropCenterX - ((float)bitmap.getWidth() / 2), cropCenterY - ((float)bitmap.getHeight() / 2));
        } else {
            canvas.translate(cropCenterX - ((float)bitmap.getHeight() / 2), cropCenterY - ((float)bitmap.getWidth() / 2));
        }

        canvas.translate(-state.cropOffsetX / state.scale, -state.cropOffsetY / state.scale);

        for (EditImageView.Layer layer : state.layers) {
            layer.draw(canvas);
        }

        canvas.restore();

        try (final FileOutputStream stream = new FileOutputStream(target)) {
            cropped.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, stream);
        } catch (IOException e) {
            Log.e("ImageCropper: unable to save and compress cropped image", e);
        }
    }
}
