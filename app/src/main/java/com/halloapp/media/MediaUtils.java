package com.halloapp.media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaUtils {

    public static int getExifOrientation(@NonNull File file) throws IOException {
        final ExifInterface exif = new ExifInterface(file);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    }

    public static Matrix fromOrientation(int exifOrientation) {
        final Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_270: {
                matrix.postRotate(270);
                break;
            }
            case ExifInterface.ORIENTATION_ROTATE_180: {
                matrix.postRotate(180);
                break;
            }
            case ExifInterface.ORIENTATION_ROTATE_90: {
                matrix.postRotate(90);
                break;
            }
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: {
                matrix.preScale(-1.0f, 1.0f);
                break;
            }
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: {
                matrix.preScale(1.0f, -1.0f);
                break;
            }
            case ExifInterface.ORIENTATION_TRANSPOSE: {
                matrix.preRotate(-90);
                matrix.preScale(-1.0f, 1.0f);
                break;
            }
            case ExifInterface.ORIENTATION_TRANSVERSE: {
                matrix.preRotate(90);
                matrix.preScale(-1.0f, 1.0f);
                break;
            }
        }

        return matrix;
    }

    public static Bitmap decode(@NonNull File file, int maxDimension) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int dimension = Math.max(options.outWidth, options.outHeight);
        options.inSampleSize = 1;
        while (dimension > maxDimension) {
            dimension /= 2;
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        final Matrix matrix = fromOrientation(getExifOrientation(file));
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void transcode(@NonNull File fileFrom, @NonNull File fileTo, int maxDimension, int quality) throws IOException {
        final Bitmap bitmap = decode(fileFrom, maxDimension);
        try (final FileOutputStream streamTo = new FileOutputStream(fileTo)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, streamTo);
        }
    }
}
