package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import com.halloapp.posts.Media;
import com.halloapp.util.Log;

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
    public static @Nullable Bitmap decodeImage(@NonNull File file, int maxDimension) throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int dimension = Math.max(options.outWidth, options.outHeight);
        if (dimension <= 0) {
            return null;
        }
        options.inSampleSize = 1;
        while (dimension > maxDimension) {
            dimension /= 2;
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return null;
        }
        final Matrix matrix = fromOrientation(getExifOrientation(file));
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
    public static @Nullable Bitmap decodeVideo(@NonNull File file, int maxDimension) throws IOException {
        final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            Log.e("MediaUtils.decodeVideo", e);
            return null;
        }
        final Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= 27) {
            bitmap = mediaMetadataRetriever.getScaledFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, maxDimension, maxDimension);
        } else {
            final Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtTime();
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
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
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
    public static @Nullable void transcodeImage(@NonNull File fileFrom, @NonNull File fileTo, int maxDimension, int quality) throws IOException {
        final Bitmap bitmap = decode(fileFrom, Media.MEDIA_TYPE_IMAGE, maxDimension);
        if (bitmap != null) {
            try (final FileOutputStream streamTo = new FileOutputStream(fileTo)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, streamTo);
            }
            bitmap.recycle();
        } else {
            throw new IOException("cannot decode image");
        }
    }

    @WorkerThread
    public static boolean shouldConvertVideo(@NonNull File file) throws IOException {
        final MediaExtractor extractor = new MediaExtractor();
        long fileLength = file.length();
        long bitrate = 0;
        try {
            extractor.setDataSource(file.getAbsolutePath());
            for (int index = 0; index < extractor.getTrackCount(); index++) {
                final MediaFormat mediaFormat = extractor.getTrackFormat(index);
                final String trackMime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (!MediaFormat.MIMETYPE_VIDEO_AVC.equals(trackMime) &&
                        !MediaFormat.MIMETYPE_VIDEO_HEVC.equals(trackMime) &&
                        !MediaFormat.MIMETYPE_AUDIO_AAC.equals(trackMime)) {
                    Log.i("MediaUtils.shouldConvertVideo track " + index + " is " + trackMime + " will convert");
                    return true;
                }
                if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) {
                    final long trackDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
                    if (trackDuration != 0) {
                        bitrate = Math.max(bitrate, 8000000L * fileLength / trackDuration);
                    }
                }
            }
        } finally {
            extractor.release();
        }
        if (bitrate > Constants.MAX_VIDEO_BITRATE) {
            Log.i("MediaUtils.shouldConvertVideo bitrate is " + bitrate + " will convert");
            return true;
        }
        Log.i("MediaUtils.shouldConvertVideo: OK to send as is " + file.getAbsolutePath());
        return false;
    }

    public static Uri getImageCaptureUri(@NonNull Context context) {
        return FileProvider.getUriForFile(context, "com.halloapp.fileprovider",
                MediaStore.getInstance(context).getImageCaptureFile());
    }
}
