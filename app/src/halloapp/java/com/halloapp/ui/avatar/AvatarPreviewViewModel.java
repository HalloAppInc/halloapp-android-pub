package com.halloapp.ui.avatar;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;

public class AvatarPreviewViewModel extends AndroidViewModel {

    final MutableLiveData<Media> media = new MutableLiveData<>();
    final MutableLiveData<TranscodeResult> smallResult = new MutableLiveData<>();
    final MutableLiveData<TranscodeResult> fullResult = new MutableLiveData<>();

    private RectF cropRect;
    private int rotation;

    AvatarPreviewViewModel(@NonNull Application application, @Nullable Uri uri) {
        super(application);
        if (uri != null) {
            loadUri(uri);
        }
    }

    public void setCropRect(RectF cropRect) {
        this.cropRect = cropRect;
    }

    public void rotate() {
        rotation = (rotation + 270) % 360;
    }

    @Override
    protected void onCleared() {
        final Media tmpMedia = getMedia();
        if (tmpMedia != null) {
            new CleanupTmpFileTask(tmpMedia).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    Media getMedia() {
        return media.getValue();
    }

    private void loadUri(@NonNull Uri uri) {
        new LoadAvatarUriTask(getApplication(), uri, media).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    LiveData<Pair<TranscodeResult, TranscodeResult>> preparePost() {
        new TranscodeTask(getMedia(), cropRect, rotation, Constants.MAX_AVATAR_DIMENSION, smallResult).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new TranscodeTask(getMedia(), cropRect, rotation, Constants.MAX_LARGE_AVATAR_DIMENSION, fullResult).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        MediatorLiveData<Pair<TranscodeResult, TranscodeResult>> ret = new MediatorLiveData<>();
        Observer<TranscodeResult> observer = r -> {
            TranscodeResult small = smallResult.getValue();
            TranscodeResult full = fullResult.getValue();
            if (small != null && full != null) {
                ret.setValue(new Pair<>(small, full));
            }
        };
        ret.addSource(smallResult, observer);
        ret.addSource(fullResult, observer);
        return ret;
    }

    public static class TranscodeResult {
        final int byteCount;
        final int width;
        final int height;
        final String hash;
        final String filePath;

        public TranscodeResult(int byteCount, int width, int height, String hash, String filePath) {
            this.byteCount = byteCount;
            this.width = width;
            this.height = height;
            this.hash = hash;
            this.filePath = filePath;
        }
    }

    public static class TranscodeTask extends AsyncTask<Void, Void, TranscodeResult> {

        private final Media media;
        private final RectF cropRect;
        private final int rotation;
        private final int maxDimension;
        private final MutableLiveData<TranscodeResult> result;

        private File transcodedFile;

        TranscodeTask(@Nullable Media media, @Nullable RectF cropRect, int rotation, int maxDimension, @NonNull MutableLiveData<TranscodeResult> result) {
            this.media = media;
            this.cropRect = cropRect;
            this.rotation = rotation;
            this.maxDimension = maxDimension;
            this.result = result;
        }

        @Override
        protected AvatarPreviewViewModel.TranscodeResult doInBackground(Void... voids) {
            if (media != null) {
                transcodedFile = FileStore.getInstance().getTmpFile("avatar-" + maxDimension);
                try {
                    return transcode(media.file, transcodedFile, cropRect, maxDimension);
                } catch (IOException | NoSuchAlgorithmException e) {
                    Log.e("failed to transcode image", e);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(AvatarPreviewViewModel.TranscodeResult success) {
            result.postValue(success);
        }

        @WorkerThread
        public AvatarPreviewViewModel.TranscodeResult transcode(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension) throws IOException, NoSuchAlgorithmException {
            final String hash;
            final int croppedHeight;
            final int croppedWidth;
            final int fileSize;

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
            final Bitmap rotatedBitmap = rotateBitmap(bitmap, rotation);
            if (rotatedBitmap != null) {
                final Bitmap croppedBitmap;
                if (cropRect != null) {
                    final Rect bitmapRect = new Rect((int)(rotatedBitmap.getWidth() * cropRect.left), (int)(rotatedBitmap.getHeight() * cropRect.top),
                            (int)(rotatedBitmap.getWidth() * cropRect.right), (int)(rotatedBitmap.getHeight() * cropRect.bottom));
                    croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), MediaUtils.getBitmapConfig(rotatedBitmap.getConfig()));
                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawBitmap(rotatedBitmap, bitmapRect, new Rect(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()), null);
                    rotatedBitmap.recycle();
                } else {
                    croppedBitmap = rotatedBitmap;
                }

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try (final FileOutputStream fos = new FileOutputStream(fileTo); final OutputStream streamTo = new DigestOutputStream(fos, md)) {
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, streamTo);
                    fileSize = (int)fileTo.length();
                    hash = StringUtils.bytesToHexString(md.digest());
                }
                croppedHeight = croppedBitmap.getHeight();
                croppedWidth = croppedBitmap.getWidth();

                croppedBitmap.recycle();

                return new AvatarPreviewViewModel.TranscodeResult(fileSize, croppedWidth, croppedHeight, hash, transcodedFile.getAbsolutePath());
            } else {
                throw new IOException("cannot decode image");
            }
        }

        private Bitmap rotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }
    }

    private static class LoadAvatarUriTask extends AsyncTask<Void, Void, Media> {

        private final Uri uri;
        private final Application application;
        private final MutableLiveData<Media> media;

        LoadAvatarUriTask(@NonNull Application application, @NonNull Uri uri, @NonNull MutableLiveData<Media> media) {
            this.application = application;
            this.uri = uri;
            this.media = media;
        }

        @Override
        protected Media doInBackground(Void... voids) {
            final Map<Uri, Integer> types = MediaUtils.getMediaTypes(application, Collections.singletonList(uri));
            @Media.MediaType int mediaType = types.get(uri);
            final File file = FileStore.getInstance().getTmpFile(RandomId.create());
            FileUtils.uriToFile(application, uri, file);
            final Size size = MediaUtils.getDimensions(file, mediaType);
            if (size != null) {
                final Media mediaItem = Media.createFromFile(mediaType, file);
                mediaItem.width = size.getWidth();
                mediaItem.height = size.getHeight();
                return mediaItem;
            } else {
                Log.e("AvatarPreviewActivity: failed to load " + uri);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Media media) {
            this.media.postValue(media);
        }
    }

    private static class CleanupTmpFileTask extends AsyncTask<Void, Void, Void> {

        private final Media media;

        CleanupTmpFileTask(@NonNull Media media) {
            this.media = media;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!media.file.delete()) {
                Log.e("failed to delete temporary file " + media.file.getAbsolutePath());
            }
            return null;
        }
    }

    public static class AvatarPreviewViewModelFactory implements ViewModelProvider.Factory {

        private final Application application;
        private final Uri uri;

        AvatarPreviewViewModelFactory(@NonNull Application application, @Nullable Uri uri) {
            this.application = application;
            this.uri = uri;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AvatarPreviewViewModel.class)) {
                //noinspection unchecked
                return (T) new AvatarPreviewViewModel(application, uri);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
