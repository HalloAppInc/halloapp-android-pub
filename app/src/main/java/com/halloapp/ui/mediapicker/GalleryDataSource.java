package com.halloapp.ui.mediapicker;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.halloapp.AppContext;
import com.halloapp.Suggestion;
import com.halloapp.content.ContentDb;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.BitmapUtils;
import com.halloapp.util.logs.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GalleryDataSource extends ItemKeyedDataSource<Long, GalleryItem> {

    @SuppressLint("InlinedApi")
    public static final String MEDIA_VOLUME = MediaStore.VOLUME_EXTERNAL;

    private static final int BURST_THRESHOLD_IN_SECONDS = 15000;
    private static final double ASSET_FACE_FACTOR = 1.5;
    private static final int PHOTO_SIZE = 224;

    final private ContentResolver contentResolver;
    final private boolean includeVideos;
    final private long cutoffTime;
    final private String suggestionId;
    final private AssetManager assetManager;
    private Interpreter interpreter;

    private static final String MEDIA_TYPE_SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "', '" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + "') AND " +
            MediaStore.Files.FileColumns.MIME_TYPE + " NOT IN ('image/svg+xml')";
    private static final String MEDIA_TYPE_IMAGES_ONLY = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN ('" +
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "') AND " +
            MediaStore.Files.FileColumns.MIME_TYPE + " NOT IN ('image/svg+xml')";
    private static final String[] MEDIA_PROJECTION = new String[] {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
    };

    public GalleryDataSource(ContentResolver contentResolver, boolean includeVideos, long cutoffTime) {
        this.contentResolver = contentResolver;
        this.includeVideos = includeVideos;
        this.cutoffTime = cutoffTime;
        this.suggestionId = null;
        this.assetManager = null;
    }

    public GalleryDataSource(ContentResolver contentResolver, boolean includeVideos, @NonNull String suggestionId, AssetManager assetManager) {
        this.contentResolver = contentResolver;
        this.includeVideos = includeVideos;
        this.cutoffTime = -1;
        this.suggestionId = suggestionId;
        this.assetManager = assetManager;
        loadModel();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<GalleryItem> callback) {
        callback.onResult(load(null, true, params.requestedLoadSize));
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<GalleryItem> callback) {
        callback.onResult(load(params.key, true, params.requestedLoadSize));
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<GalleryItem> callback) {
        callback.onResult(load(params.key, false, params.requestedLoadSize));
    }

    // Tensorflow example: https://github.com/tensorflow/examples/blob/f67a01daeb5cc34b1c5a3a2ee25c977321a86280/lite/examples/model_personalization/android/transfer_api/src/main/java/org/tensorflow/lite/examples/transfer/api/ModelLoader.java#L45
    private void loadModel() {
        BgWorkers.getInstance().execute(() -> {
            try {
                AssetFileDescriptor fileDescriptor = assetManager.openFd("aesthetic_scoring_model.tflite");
                FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declareLength = fileDescriptor.getDeclaredLength();
                interpreter = new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength));
                fileChannel.close();
            } catch (IOException e) {
                Log.e("Failed to load model", e);
            }
        });
    }

    private void scoreGalleryItems(@NonNull List<GalleryItem> galleryItems) {
        BgWorkers.getInstance().execute(() -> {
            for (GalleryItem galleryItem : galleryItems) {
                try {
                    Bitmap bitmap;
                    if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                        // TODO(michelle): investigate the region getThumbnail returns (should be centered)
                        bitmap = Build.VERSION.SDK_INT < 29
                                ? MediaStore.Images.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Images.Thumbnails.MINI_KIND, null)
                                : contentResolver.loadThumbnail(ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id), new Size(PHOTO_SIZE, PHOTO_SIZE), null);
                    } else {
                        bitmap = Build.VERSION.SDK_INT < 29
                                ? MediaStore.Video.Thumbnails.getThumbnail(contentResolver, galleryItem.id, MediaStore.Video.Thumbnails.MINI_KIND, null)
                                : contentResolver.loadThumbnail(ContentUris.withAppendedId(MediaStore.Video.Media.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id), new Size(PHOTO_SIZE, PHOTO_SIZE), null);
                    }
                    galleryItem.score = runModel(bitmap);
                } catch (IOException e) {
                    Log.e("Could not find media file ", e);
                }
            }
            List<Long> suggestedGalleryItems = findBestOfBursts(galleryItems);
            ContentDb.getInstance().markSuggestedGalleryItems(suggestedGalleryItems, suggestionId);
        });
    }

    // Using the aesthetic model: https://github.com/idealo/image-quality-assessment
    @WorkerThread
    private float runModel(@Nullable Bitmap bitmap) {
        float score = 0;
        if (interpreter == null || bitmap == null) {
            return score;
        }
        try {
            // Input to the model is a 224x224 array of RGB values
            float[][][][] input = new float[1][PHOTO_SIZE][PHOTO_SIZE][3];
            input[0] = BitmapUtils.getRGBArray(bitmap, PHOTO_SIZE, PHOTO_SIZE);
            /// Output[0][i] is probability an image should receive score i+1 (scores from 1-10 with 10 being the most aesthetic)
            float[][] output = new float[1][10];
            interpreter.run(input, output);
            for (int i = 0; i < output[0].length; i++) {
                score += (i + 1) * output[0][i];
            }
            // Run face detection model and boost base score by ASSET_FACE_FACTOR if faces are found
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            FaceDetector detector = FaceDetection.getClient();
            Task<List<Face>> task = detector.process(image);
            Tasks.await(task);
            List<Face> faces = task.isSuccessful() ? task.getResult() : null;
            // TODO(michelle): explore other heuristics involving face detection (size, num, etc.)
            if (faces != null && !faces.isEmpty()) {
                score += ASSET_FACE_FACTOR;
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("Exception when running face detector ", e);
        }
        return score;
    }

    private List<Long> findBestOfBursts(@NonNull List<GalleryItem> galleryItems) {
        List<Long> bestOfBursts = new ArrayList<>();
        if (galleryItems.isEmpty()) {
            return bestOfBursts;
        }
        GalleryItem bestItem = galleryItems.get(0);
        long currentBurstTime = galleryItems.get(0).date;
        for (int i = 1; i < galleryItems.size(); i++) {
            GalleryItem currentItem = galleryItems.get(i);
            // Choose best photo within a burst (photo taken less than BurstThreshold apart)
            if (currentBurstTime - currentItem.date < BURST_THRESHOLD_IN_SECONDS) {
                bestItem = currentItem.score > bestItem.score ? currentItem : bestItem;
            } else {
                bestOfBursts.add(bestItem.id);
                bestItem = currentItem;
            }
            currentBurstTime = bestItem.date;
        }
        bestOfBursts.add(bestItem.id);
        return bestOfBursts;
    }

    public List<GalleryItem> load(Long start, boolean after, int size) {
        if (suggestionId != null) {
            List<GalleryItem> galleryItems = ContentDb.getInstance().getGalleryItemsBySuggestion(suggestionId);
            Suggestion suggestion = ContentDb.getInstance().getSuggestion(suggestionId);
            if (suggestion != null && !suggestion.isScored) {
                scoreGalleryItems(galleryItems);
            }
            return galleryItems;
        }
        final List<GalleryItem> galleryItems = new ArrayList<>();
        try (final Cursor cursor = getCursor(start, after, size)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    int type = cursor.getInt(1);
                    long duration = 0;
                    long date = cursor.getLong(2);

                    if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        if (Build.VERSION.SDK_INT >= 29) {
                            duration = cursor.getLong(3);
                        } else {
                            duration = getDuration(id);
                        }
                    }

                    galleryItems.add(new GalleryItem(id, type, date, duration));
                }
            }
        } catch (SecurityException ex) {
            Log.w("GalleryDataSource.load", ex);
        }
        return galleryItems;
    }

    private Cursor getCursor(Long start, boolean after, int size) {
        String[] projection;
        if (Build.VERSION.SDK_INT >= 29) {
            projection = Arrays.copyOf(MEDIA_PROJECTION, MEDIA_PROJECTION.length + 1);
            projection[projection.length - 1] = MediaStore.Files.FileColumns.DURATION;
        } else {
            projection = MEDIA_PROJECTION;
        }

        String selection = (includeVideos ? MEDIA_TYPE_SELECTION : MEDIA_TYPE_IMAGES_ONLY) + (start == null ? "" : ((after ? " AND _id<" : " AND _id>") + start)) + " AND " + MediaStore.MediaColumns.DATE_ADDED + ">?";
        Uri uri = MediaStore.Files.getContentUri(MEDIA_VOLUME);
        if (Build.VERSION.SDK_INT >= 30) { // Adding LIMIT in the sortOrder field like below causes a crash starting on API 30
            Bundle queryArgs = new Bundle();
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[] {Long.toString(cutoffTime)});
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "_id DESC");
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, Integer.toString(size));
            return contentResolver.query(uri, projection, queryArgs, null);
        } else {
            return contentResolver.query(uri, projection, selection, new String[] {Long.toString(cutoffTime)}, "_id DESC LIMIT " + size, null);
        }
    }

    private long getDuration(long id) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(MEDIA_VOLUME), id);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(AppContext.getInstance().get(), uri);
            String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationString == null) {
                Log.w("GalleryDataSource.getDuration got null duration");
                return 0;
            }
            return Long.parseLong(durationString);
        } catch (IllegalArgumentException e) {
            Log.e("GalleryDataSource.getDuration invalid uri " + uri + " for id " + id, e);
            return 0;
        } catch (RuntimeException e) {
            Log.e("GalleryDataSource.getDuration other exception with uri " + uri + " for id " + id, e);
            return 0;
        } finally  {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e("GalleryDataSource.getDuration retriever release failed", e);
            }
        }
    }

    @Override
    public @NonNull Long getKey(@NonNull GalleryItem item) {
        return item.id;
    }


    public static class Factory extends DataSource.Factory<Long, GalleryItem> {

        final ContentResolver contentResolver;
        final boolean includeVideos;
        private GalleryDataSource latestSource;

        public Factory(ContentResolver contentResolver, boolean includeVideos) {
            this.contentResolver = contentResolver;
            this.includeVideos = includeVideos;
            latestSource = new GalleryDataSource(contentResolver, includeVideos, -1);
        }

        public Factory(ContentResolver contentResolver, boolean includeVideos, @NonNull String suggestionId, AssetManager assetManager) {
            this.contentResolver = contentResolver;
            this.includeVideos = includeVideos;
            latestSource = new GalleryDataSource(contentResolver, includeVideos, suggestionId, assetManager);
        }

        @Override
        public @NonNull DataSource<Long, GalleryItem> create() {
            if (latestSource.isInvalid()) {
                Log.i("GalleryDataSource.Factory.create old source was invalidated; creating a new one");
                latestSource = new GalleryDataSource(contentResolver, includeVideos, -1);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("GalleryDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }
}
