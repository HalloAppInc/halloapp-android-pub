package com.halloapp.media;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.MediaBox;
import org.mp4parser.boxes.iso14496.part12.MediaHeaderBox;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.RandomAccessSource;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class MediaUtils {
    private static final int TRIMMING_BUFFER_SIZE = 2 * 1024 * 1024;
    private static final long INITIAL_FRAME_TIME = 1000;

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

    private static int computeMaxDimension(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels));
    }

    @WorkerThread
    public static @Nullable Bitmap decode(@NonNull Context context, @NonNull File file, @Media.MediaType int mediaType) throws IOException {
        return decode(file, mediaType, computeMaxDimension(context));
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
            case Media.MEDIA_TYPE_AUDIO:
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
    public static @Nullable Bitmap decodeImage(Context context, @NonNull File file) throws IOException {
        return decodeImage(file, computeMaxDimension(context));
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

        long initialFrameTime = INITIAL_FRAME_TIME;
        long videoDuration = getVideoDuration(file);
        if (videoDuration < INITIAL_FRAME_TIME) {
            initialFrameTime = videoDuration / 2;
        }

        final Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= 27) {
            bitmap = mediaMetadataRetriever.getScaledFrameAtTime(initialFrameTime * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, maxDimension, maxDimension);
        } else {
            final Bitmap frameBitmap = mediaMetadataRetriever.getFrameAtTime(initialFrameTime * 1000);
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
            case Media.MEDIA_TYPE_AUDIO:
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
            try {
                mediaMetadataRetriever.release();
            } catch (IOException e) {
                Log.e("MediaUtils.getVideoDimensions retriever release failed", e);
            }
        }
        return size;
    }

    @WorkerThread
    public static void transcodeImage(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension, int quality, boolean forcesRGB) throws IOException {
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
                croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), getBitmapConfig(bitmap.getConfig(), forcesRGB));
                final Canvas canvas = new Canvas(croppedBitmap);
                canvas.drawColor(0xffffffff); // white background in case image has transparency
                canvas.drawBitmap(bitmap, bitmapRect, new Rect(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()), null);
                bitmap.recycle();
            } else {
                if (bitmap.getPixel(0,0) == 0 || forcesRGB) {
                    // white background in case image has transparency
                    croppedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), getBitmapConfig(bitmap.getConfig(), forcesRGB));
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

    private static int getPreferredMaxVideoBitrate(boolean isSelfie) {
        final ServerProps serverProps = ServerProps.getInstance();
        final int overrideMaxVideoBitrate = Constants.VIDEO_BITRATE_OVERRIDE;
        if ((serverProps.getIsInternalUser() || BuildConfig.DEBUG) && overrideMaxVideoBitrate > 0) {
            return overrideMaxVideoBitrate;
        } else {
            return isSelfie ? serverProps.getMaxSelfieVideoBitrate() : serverProps.getMaxVideoBitrate();
        }
    }

    @WorkerThread
    public static boolean shouldConvertVideo(@NonNull File file, long maxVideoDurationSeconds, boolean isSelfie, @NonNull String mediaLogId) throws IOException {
        final int maxBitrate = getPreferredMaxVideoBitrate(isSelfie);
        Log.i("MediaUtils.shouldConvertVideo maxBitrate is " + maxBitrate + " maxVideoDurationSeconds is " + maxVideoDurationSeconds + " for " + mediaLogId);
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
                    Log.i("MediaUtils.shouldConvertVideo track " + index + " is " + trackMime + ", will convert " + mediaLogId);
                    return true;
                }
                if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) {
                    final long trackDurationMs = mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000;
                    if (trackDurationMs != 0) {
                        final double trackDurationSeconds = trackDurationMs / 1000.0;
                        bitrate = Math.max(bitrate, (long) (8 * fileLength / trackDurationSeconds));
                        duration = Math.max(duration, trackDurationMs);
                    }
                }
            }
        } finally {
            extractor.release();
        }
        if (bitrate > maxBitrate) {
            Log.i("MediaUtils.shouldConvertVideo bitrate is " + bitrate + ", will convert " + mediaLogId);
            return true;
        }
        if (duration > maxVideoDurationSeconds * 1000) {
            Log.i("MediaUtils.shouldConvertVideo duration is " + duration + ", will convert " + mediaLogId);
            return true;
        }
        Log.i("MediaUtils.shouldConvertVideo: OK to send as is " + file.getAbsolutePath() + " for " + mediaLogId);
        return false;
    }

    public static Uri getImageCaptureUri(@NonNull Context context) {
        return FileProvider.getUriForFile(context, "com.halloapp.fileprovider",
                FileStore.getInstance().getImageCaptureFile());
    }

    public static @NonNull Bitmap getCircledBitmap(@NonNull Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), getBitmapConfig(bitmap.getConfig()));
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static @NonNull Map<Uri, Integer> getMediaTypes(@NonNull Context context, @NonNull Collection<Uri> uris) {
        uris = new ArrayList<>(uris);
        List<Long> list = new ArrayList<>();
        HashMap<Uri, Integer> types = new HashMap<>();

        for (Uri uri : uris) {
            try {
                list.add(ContentUris.parseId(uri));
            } catch (NumberFormatException | UnsupportedOperationException ex) {
                // Not a url we can handle here, will use mimeTypeMap below instead.
                Log.w("MediaUtils.getMediaTypes", ex);
            }
        }

        ContentResolver resolver = context.getContentResolver();
        try (final Cursor cursor = resolver.query(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME),
                new String[] {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE,},
                MediaStore.Files.FileColumns._ID + " in (" + TextUtils.join(",", list) + ")",
                null,
                null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), cursor.getLong(0));
                    types.put(uri, Media.MEDIA_TYPE_UNKNOWN);

                    switch (cursor.getInt(1)) {
                        case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
                            types.put(uri, Media.MEDIA_TYPE_IMAGE);
                            break;
                        case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
                            types.put(uri, Media.MEDIA_TYPE_VIDEO);
                            break;
                        default:
                            types.put(uri, Media.MEDIA_TYPE_UNKNOWN);
                            break;
                    }
                }
            }
        } catch (SecurityException ex) {
            Log.w("MediaUtils.getMediaTypes", ex);
        }

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        for (Uri uri: uris) {
            if (!types.containsKey(uri)) {
                if (Objects.equals(uri.getScheme(), "file")) {
                    String mime = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
                    types.put(uri, Media.getMediaType(mime));
                } else {
                    types.put(uri, Media.getMediaType(resolver.getType(uri)));
                }
            }
        }

        return types;
    }

    public static @NonNull Map<Uri, Long> getDates(@NonNull Context context, @NonNull Collection<Uri> uris) {
        List<Long> list = new ArrayList<>();
        HashMap<Uri, Long> dates = new HashMap<>();

        for (Uri uri : uris) {
            try {
                dates.put(uri, 0L);
                list.add(ContentUris.parseId(uri));
            } catch (NumberFormatException | UnsupportedOperationException ex) {
                // Not a url we can handle here, will use 0 as date.
                Log.w("MediaUtils.getDates", ex);
            }
        }

        String selection = MediaStore.Files.FileColumns._ID + " in (" + TextUtils.join(",", list) + ")";
        final String[] projection = new String[] {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
        };

        try(final Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME),
                projection,
                selection,
                null,
                null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), id);
                    dates.put(uri, cursor.getLong(1));
                }
            }
        } catch (SecurityException ex) {
            Log.w("MediaUtils.getDates", ex);
        }

        return dates;
    }

    public static @NonNull Bitmap.Config getBitmapConfig(@Nullable Bitmap.Config config) {
        return getBitmapConfig(config, false);
    }

    public static @NonNull Bitmap.Config getBitmapConfig(@Nullable Bitmap.Config config, boolean forcesRGB) {
        if (config != Bitmap.Config.ARGB_8888 && Build.VERSION.SDK_INT >= 26 && !forcesRGB) {
            return Bitmap.Config.RGBA_F16; // Wide color gamut
        } else {
            return Bitmap.Config.ARGB_8888; // sRGB
        }
    }

    @WorkerThread
    public static long getAudioDuration(@NonNull File file) {
        if (Build.VERSION.SDK_INT >= 29) {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(file.getAbsolutePath());
                return Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            } catch (IllegalArgumentException | IOException e) {
                Log.e("MediaUtils.getAudioDuration", e);
                return 0;
            }
        } else {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                retriever.release();
                return duration;
            } catch (IllegalArgumentException | IOException e) {
                Log.e("MediaUtils.getAudioDuration", e);
                return 0;
            }
        }
    }

    public static boolean saveMediaToGallery(@NonNull Context context, @NonNull Media media) {
        return saveMediaToGallery(context, media.file, media.type);
    }

    public static boolean saveMediaToGallery(@NonNull Context context, @Nullable File file, @Media.MediaType int type) {
        if (file == null || !file.exists()) {
            return false;
        }
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri contentUri;
        switch (type) {
            case Media.MEDIA_TYPE_IMAGE: {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.TITLE, file.getName());
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            }
            case Media.MEDIA_TYPE_VIDEO: {
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.TITLE, file.getName());
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            }
            default:
                return false;
        }
        Uri uri = contentResolver.insert(contentUri, values);
        if (uri == null) {
            return false;
        }
        try (OutputStream os = contentResolver.openOutputStream(uri)) {
            if (os == null) {
                return false;
            }
            try (InputStream is = new FileInputStream(file)) {
                FileUtils.copyFile(is, os);
            }
            return true;
        } catch (IOException e) {
            Log.e("MediaUtils/saveMediaToGallery failed to save media to gallery", e);
            return false;
        }
    }

    public static long getVideoDuration(@NonNull File file) {
        if (Build.VERSION.SDK_INT >= 29) {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(file.getAbsolutePath());
                return Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            } catch (IllegalArgumentException | IOException e) {
                Log.e("MediaUtils.getVideoDuration", e);
                return 0;
            }
        } else {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                retriever.release();
                return duration;
            } catch (IllegalArgumentException | IOException e) {
                Log.e("MediaUtils.getVideoDuration", e);
                return 0;
            }
        }
    }

    @WorkerThread
    public static void reconstructVideoContainer(@NonNull File videoFile) throws IOException {
        Log.i("MediaUtils.reconstructVideoContainer file " + videoFile.getAbsolutePath() + " of size " + videoFile.length());
        final Date zeroDate = new Date(0);
        try (FileInputStream fileInputStream = new FileInputStream(videoFile)) {
            try (RandomAccessSource randomAccessSource = new FileRandomAccessSourceImpl(new RandomAccessFile(videoFile, "r"))) {
                final Movie inputMovie = MovieCreator.build(fileInputStream.getChannel(), randomAccessSource, "video");
                final Mp4Builder mp4Builder = new StreamableMp4Builder();
                final Container container = mp4Builder.build(inputMovie);

                MovieBox movieBox = null;
                for (Box box : container.getBoxes()) {
                    if (box instanceof MovieBox) {
                        movieBox = (MovieBox) box;
                        break;
                    }
                }
                if (movieBox != null) {
                    MovieHeaderBox movieHeaderBox = movieBox.getMovieHeaderBox();
                    if (movieHeaderBox != null) {
                        movieHeaderBox.setCreationTime(zeroDate);
                        movieHeaderBox.setModificationTime(zeroDate);
                    }

                    for (Box box : movieBox.getBoxes()) {
                        if (box instanceof TrackBox) {
                            TrackBox trackBox = (TrackBox) box;
                            TrackHeaderBox trackHeaderBox = trackBox.getTrackHeaderBox();
                            if (trackHeaderBox != null) {
                                trackHeaderBox.setCreationTime(zeroDate);
                                trackHeaderBox.setModificationTime(zeroDate);
                            }
                            MediaBox mediaBox = trackBox.getMediaBox();
                            if (mediaBox != null) {
                                MediaHeaderBox mediaHeaderBox = trackBox.getMediaBox().getMediaHeaderBox();
                                if (mediaHeaderBox != null) {
                                    mediaHeaderBox.setCreationTime(zeroDate);
                                    mediaHeaderBox.setModificationTime(zeroDate);
                                }
                            }
                        }
                    }
                }

                final File tempFile = FileStore.getInstance().getTmpFile(RandomId.create());
                try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                    container.writeContainer(fileOutputStream.getChannel());
                } catch (IOException e) {
                    tempFile.delete();
                    throw e;
                }

                if (!videoFile.delete()) {
                    Log.e("MediaUtils.zeroVideoTimestampMetadata: failed to delete " + videoFile.getAbsolutePath());
                }
                if (!tempFile.renameTo(videoFile)) {
                    Log.e("MediaUtils.zeroVideoTimestampMetadata: failed to rename " + tempFile.getAbsolutePath() + " to " + videoFile.getAbsolutePath());
                } else {
                    tempFile.delete();
                }
            }
        }
    }

    @WorkerThread
    public static void trimVideo(@NonNull Context context, @NonNull File file, @NonNull File target, long startTimeUs, long endTimeUs, boolean mute) throws IOException {
        trimVideo(context, Uri.fromFile(file), target, startTimeUs, endTimeUs, mute);
    }

    @WorkerThread
    public static void trimVideo(@NonNull Context context, @NonNull Uri uri, @NonNull File target, long startTimeUs, long endTimeUs, boolean mute) throws IOException {
        int rotationDegrees = 0;
        int bufferSize = -1;
        HashMap<Integer, Integer> indexes = new HashMap<>();

        MediaMuxer muxer = new MediaMuxer(target.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(context, uri, null);

        for (int i = 0; i < extractor.getTrackCount(); ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mute && mime != null && mime.startsWith("audio/")) {
                continue;
            }

            if (Build.VERSION.SDK_INT >= 23 && format.containsKey(MediaFormat.KEY_ROTATION)) {
                rotationDegrees = format.getInteger(MediaFormat.KEY_ROTATION);
            }

            extractor.selectTrack(i);
            indexes.put(i, muxer.addTrack(format));

            if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                bufferSize = Math.max(format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE), bufferSize);
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize < 0 ? TRIMMING_BUFFER_SIZE : bufferSize);

        extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

        int offset = 0;
        muxer.setOrientationHint(rotationDegrees);
        muxer.start();
        while (true) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            try {
                info.offset = offset;
                info.size = extractor.readSampleData(buffer, offset);
                info.presentationTimeUs = extractor.getSampleTime();

                boolean isKeyFrame = (extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0;
                info.flags = isKeyFrame ? MediaCodec.BUFFER_FLAG_KEY_FRAME : 0;
            } catch (IllegalArgumentException e) {
                Log.e("VideoEditActivity: unable to extract video, probably buffer too small", e);
                throw new IOException(e);
            }

            if (info.size < 0 || (endTimeUs > 0 && info.presentationTimeUs > endTimeUs)) {
                break;
            }

            muxer.writeSampleData(Objects.requireNonNull(indexes.get(extractor.getSampleTrackIndex())), buffer, info);
            extractor.advance();
        }

        muxer.stop();
        extractor.release();
        muxer.release();
    }

    @WorkerThread
    public static void cropImage(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension) throws IOException {
        final int maxWidth;
        final int maxHeight;

        if (cropRect != null) {
            maxWidth = (int)(maxDimension / cropRect.width());
            maxHeight =(int)(maxDimension / cropRect.height());
        } else {
            maxWidth = maxDimension;
            maxHeight = maxDimension;
        }

        final Bitmap bitmap = MediaUtils.decodeImage(fileFrom, maxWidth, maxHeight);

        if (bitmap != null) {
            final Bitmap croppedBitmap;
            if (cropRect != null) {
                final Rect bitmapRect = new Rect((int)(bitmap.getWidth() * cropRect.left), (int)(bitmap.getHeight() * cropRect.top),
                        (int)(bitmap.getWidth() * cropRect.right), (int)(bitmap.getHeight() * cropRect.bottom));
                croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), MediaUtils.getBitmapConfig(bitmap.getConfig()));
                final Canvas canvas = new Canvas(croppedBitmap);
                canvas.drawBitmap(bitmap, bitmapRect, new Rect(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()), null);
                bitmap.recycle();
            } else {
                croppedBitmap = bitmap;
            }

            try (final FileOutputStream output = new FileOutputStream(fileTo)) {
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, output);
            }

            croppedBitmap.recycle();
        } else {
            throw new IOException("cannot decode image");
        }
    }
}
