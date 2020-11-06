package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class MediaUtils {

    @WorkerThread
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

    static boolean swapsWidthAnHeight(int exifOrientation) {
        return
                exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                exifOrientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                exifOrientation == ExifInterface.ORIENTATION_TRANSVERSE;
    }

    @WorkerThread
    public static @Nullable Bitmap decode(@NonNull File file, @Media.MediaType int mediaType, int maxDimension) throws IOException {
        switch (mediaType) {
            case Media.MEDIA_TYPE_IMAGE: {
                return decodeImage(file, maxDimension);
            }
            case Media.MEDIA_TYPE_VIDEO: {
                return decodeVideo(file, maxDimension);
            }
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                return null;
            }
        }
    }

    @WorkerThread
    public static void createThumb(@NonNull File fileFrom, @NonNull File fileTo, @Media.MediaType int mediaType, int maxDimension) throws IOException {
        final Bitmap thumb = decode(fileFrom, mediaType, maxDimension);
        if (thumb == null) {
            throw new IOException("cannot decode " + fileFrom.getAbsolutePath());
        }
        try (final FileOutputStream streamTo = new FileOutputStream(fileTo)) {
            thumb.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, streamTo);
        }
        thumb.recycle();
    }

    @WorkerThread
    public static @Nullable Bitmap decodeImage(@NonNull File file, int maxDimension) throws IOException {
        return decodeImage(file, maxDimension, maxDimension);
    }

    @WorkerThread
    public static @Nullable Bitmap decodeImage(@NonNull File file, int maxWidth, int maxHeight) throws IOException {
        final int exifOrientation = getExifOrientation(file);
        if (swapsWidthAnHeight(exifOrientation)) {
            final int tmp = maxWidth;
            //noinspection SuspiciousNameCombination
            maxWidth = maxHeight;
            maxHeight = tmp;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int width = options.outWidth;
        int height = options.outHeight;
        if (width <= 0 || height <= 0) {
            return null;
        }
        options.inSampleSize = 1;
        // allow image to be scaled to 10% less than max size requested, if this can be done by simple downsampling;
        // this saves memory, increases speed, and would not reduce quality much, because no further bi-linear scaling would be needed
        while (1.1f * width > 2 * maxWidth || 1.1f * height > 2 * maxHeight) {
            width /= 2;
            height /= 2;
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return null;
        }
        float scale = Math.min(1f * maxWidth / bitmap.getWidth(), 1f * maxHeight / bitmap.getHeight());
        final Matrix matrix = fromOrientation(exifOrientation);
        if (scale < 1) {
            matrix.preScale(scale, scale);
        }
        if (matrix.isIdentity()) {
            return bitmap;
        } else {
            final Bitmap transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (transformedBitmap != bitmap) {
                bitmap.recycle();
            }
            return transformedBitmap;
        }
    }

    @WorkerThread
    public static @Nullable Bitmap decodeVideo(@NonNull File file, int maxDimension) {
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            Log.e("MediaUtils.decodeVideo", e);
            return null;
        }
        final Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= 27) {
            bitmap = mediaMetadataRetriever.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, maxDimension, maxDimension);
        } else {
            final Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtTime(0);
            if (frameBitmap != null && (frameBitmap.getWidth() > maxDimension || frameBitmap.getHeight() > maxDimension)) {
                final float scale = Math.min(1f * maxDimension / frameBitmap.getWidth(), 1f * maxDimension / frameBitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(frameBitmap, (int) (frameBitmap.getWidth() * scale), (int) (frameBitmap.getHeight() * scale), true);
                if (frameBitmap != bitmap) {
                    frameBitmap.recycle();
                }
            } else {
                bitmap = frameBitmap;
            }
        }
        return bitmap;
    }

    @WorkerThread
    public static @Nullable Size getDimensions(@NonNull File file, @Media.MediaType int mediaType) {
        switch (mediaType) {
            case Media.MEDIA_TYPE_IMAGE: {
                return getImageDimensions(file);
            }
            case Media.MEDIA_TYPE_VIDEO: {
                return getVideoDimensions(file);
            }
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                return null;
            }
        }
    }

    @WorkerThread
    public static @Nullable Size getImageDimensions(@NonNull File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        final Size size;
        if (options.outHeight > 0 && options.outWidth > 0) {
            int orientation = ExifInterface.ORIENTATION_UNDEFINED;
            try {
                orientation = MediaUtils.getExifOrientation(file);
            } catch (IOException ignore) {
            }
            if (swapsWidthAnHeight(orientation)) {
                //noinspection SuspiciousNameCombination
                size = new Size(options.outHeight, options.outWidth);
            } else {
                size = new Size(options.outWidth, options.outHeight);
            }
        } else {
            size = null;
        }
        return size;
    }

    @WorkerThread
    public static @Nullable Size getVideoDimensions(@NonNull File file) {
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        final Size size;
        try {
            try {
                mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                Log.e("MediaUtils.getVideoDimensions", e);
                return null;
            }
            final String rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            final String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            final String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            try {
                if ("90".equals(rotation) || "270".equals(rotation)) {
                    size = new Size(Integer.parseInt(height), Integer.parseInt(width));
                } else {
                    size = new Size(Integer.parseInt(width), Integer.parseInt(height));
                }
            } catch (NumberFormatException e) {
                Log.e("MediaUtils.getVideoDimensions", e);
                return null;
            }
        } finally {
            mediaMetadataRetriever.release();
        }
        return size;
    }

    @WorkerThread
    public static void transcodeImage(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension, int quality) throws IOException {
        final int maxWidth;
        final int maxHeight;
        if (cropRect != null) {
            maxWidth = (int)(maxDimension / cropRect.width());
            maxHeight =(int)(maxDimension / cropRect.height());
        } else {
            maxWidth = maxDimension;
            maxHeight = maxDimension;
        }
        final Bitmap bitmap = decodeImage(fileFrom, maxWidth, maxHeight);
        if (bitmap != null) {
            final Bitmap croppedBitmap;
            if (cropRect != null && !(cropRect.left == 0 && cropRect.top == 0 && cropRect.right == 1 && cropRect.bottom == 1)) {
                final Rect bitmapRect = new Rect((int)(bitmap.getWidth() * cropRect.left), (int)(bitmap.getHeight() * cropRect.top),
                        (int)(bitmap.getWidth() * cropRect.right), (int)(bitmap.getHeight() * cropRect.bottom));
                croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(croppedBitmap);
                canvas.drawColor(0xffffffff); // white background in case image has transparency
                canvas.drawBitmap(bitmap, bitmapRect, new Rect(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()), null);
                bitmap.recycle();
            } else {
                if (bitmap.getPixel(0,0) == 0) {
                    // white background in case image has transparency
                    croppedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawColor(0xffffffff);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    bitmap.recycle();
                } else {
                    croppedBitmap = bitmap;
                }
            }
            try (final FileOutputStream streamTo = new FileOutputStream(fileTo)) {
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, streamTo);
            }
            croppedBitmap.recycle();
        } else {
            throw new IOException("cannot decode image");
        }
    }

    @WorkerThread
    public static boolean shouldConvertVideo(@NonNull File file) throws IOException {
        final MediaExtractor extractor = new MediaExtractor();
        long fileLength = file.length();
        long bitrate = 0;
        long duration = 0;
        try {
            extractor.setDataSource(file.getAbsolutePath());
            for (int index = 0; index < extractor.getTrackCount(); index++) {
                final MediaFormat mediaFormat = extractor.getTrackFormat(index);
                final String trackMime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (!MediaFormat.MIMETYPE_VIDEO_AVC.equals(trackMime) &&
                        !MediaFormat.MIMETYPE_VIDEO_HEVC.equals(trackMime) &&
                        !MediaFormat.MIMETYPE_AUDIO_AAC.equals(trackMime)) {
                    Log.i("MediaUtils.shouldConvertVideo track " + index + " is " + trackMime + ", will convert");
                    return true;
                }
                if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) {
                    final long trackDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
                    if (trackDuration != 0) {
                        bitrate = Math.max(bitrate, Constants.MAX_VIDEO_BITRATE * fileLength / trackDuration);
                        duration = Math.max(duration, trackDuration / 1000L);
                    }
                }
            }
        } finally {
            extractor.release();
        }
        if (bitrate > Constants.MAX_VIDEO_BITRATE) {
            Log.i("MediaUtils.shouldConvertVideo bitrate is " + bitrate + ", will convert");
            return true;
        }
        if (duration > Constants.MAX_VIDEO_DURATION) {
            Log.i("MediaUtils.shouldConvertVideo duration is " + duration + ", will convert");
            return true;
        }
        Log.i("MediaUtils.shouldConvertVideo: OK to send as is " + file.getAbsolutePath());
        return false;
    }

    public static Uri getImageCaptureUri(@NonNull Context context) {
        return FileProvider.getUriForFile(context, "com.halloapp.fileprovider",
                FileStore.getInstance().getImageCaptureFile());
    }

    public static @NonNull Bitmap getCircledBitmap(@NonNull Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
}
