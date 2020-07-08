package com.halloapp.ui.avatar;

import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Media;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.CropPhotoView;
import com.halloapp.xmpp.Connection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class AvatarPreviewActivity extends HalloActivity {

    private AvatarPreviewViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private CropPhotoView imageView;
    private RectF cropRect;
    private int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AvatarPreviewActivity: onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_set_avatar);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        final View setButton = findViewById(R.id.done);
        setButton.setOnClickListener(v -> viewModel.preparePost());

        imageView = findViewById(R.id.image);

        final View resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> imageView.getAttacher().update());

        final View progressView = findViewById(R.id.progress);

        final Uri uri = getIntent().getData();
        if (uri != null) {
            progressView.setVisibility(View.VISIBLE);
            setButton.setEnabled(false);

            viewModel = new ViewModelProvider(this, new AvatarPreviewViewModelFactory(getApplication(), uri)).get(AvatarPreviewViewModel.class);
            viewModel.media.observe(this, mediaItem -> {
                progressView.setVisibility(View.GONE);
                if (mediaItem != null) {
                    imageView.setSinglePointerDragStartDisabled(false);
                    imageView.setReturnToMinScaleOnUp(false);
                    imageView.setOnCropListener(rect -> cropRect = rect);
                    imageView.setGridEnabled(false);
                    mediaThumbnailLoader.load(imageView, mediaItem);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    CenterToast.show(getBaseContext(), R.string.failed_to_load_media);
                }
                setButton.setEnabled(true);
            });
        } else {
            Log.e("AvatarPreviewActivity no uris provided");
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AvatarPreviewActivity: onDestroy");
        mediaThumbnailLoader.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.profile_photo_crop_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.rotate: {
                imageView.setRotationBy(270);
                rotation = (rotation + 270) % 360;
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    static class LoadAvatarUriTask extends AsyncTask<Void, Void, Media> {

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
            final ContentResolver contentResolver = application.getContentResolver();
            @Media.MediaType int mediaType = Media.getMediaType(contentResolver.getType(uri));
            final File file = FileStore.getInstance(application).getTmpFile(RandomId.create());
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

    static class CleanupTmpFileTask extends AsyncTask<Void, Void, Void> {

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

    class Prepare extends AsyncTask<Void, Void, Boolean> {

        private final Media media;
        private final Application application;

        Prepare(@NonNull Application application, @Nullable Media media) {
            this.application = application;
            this.media = media;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (media != null) {
                final File tmpFile = FileStore.getInstance(application).getTmpFile("avatar");
                try {
                    TranscodeResult transcodeResult = transcode(media.file, tmpFile, cropRect, Constants.MAX_AVATAR_DIMENSION);
                    String avatarId = uploadAvatar(tmpFile, Connection.getInstance(), transcodeResult);
                    if (avatarId == null) {
                        return false;
                    }

                    final File outFile = FileStore.getInstance(application).getAvatarFile(UserId.ME.rawId());
                    FileUtils.copyFile(tmpFile, outFile);

                    AvatarLoader avatarLoader = AvatarLoader.getInstance(AvatarPreviewActivity.this);
                    avatarLoader.reportMyAvatarChanged(avatarId);
                } catch (IOException | NoSuchAlgorithmException e) {
                    Log.e("failed to transcode image", e);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success != null && success) {
                setResult(RESULT_OK);
                finish();
            } else {
                CenterToast.show(getApplicationContext(), R.string.could_not_set_avatar);
            }
        }

        private class TranscodeResult {
            final int byteCount;
            final int width;
            final int height;
            final String hash;

            public TranscodeResult(int byteCount, int width, int height, String hash) {
                this.byteCount = byteCount;
                this.width = width;
                this.height = height;
                this.hash = hash;
            }
        }

        @WorkerThread
        public TranscodeResult transcode(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension) throws IOException, NoSuchAlgorithmException {
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
                    croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), Bitmap.Config.ARGB_8888);
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

                return new TranscodeResult(fileSize, croppedWidth, croppedHeight, hash);
            } else {
                throw new IOException("cannot decode image");
            }
        }

        private Bitmap rotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        @WorkerThread
        public String uploadAvatar(File file, Connection connection, final TranscodeResult transcodeResult) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int c;
                while ((c = fileInputStream.read(buf)) != -1) {
                    baos.write(buf, 0, c);
                }
                String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                return connection.setAvatar(base64, transcodeResult.byteCount, transcodeResult.width, transcodeResult.height).get();
            } catch (IOException e) {
                Log.e("Failed to get base64", e);
            } catch (InterruptedException | ExecutionException e) {
                Log.e("Avatar upload interrupted", e);
            }
            return null;
        }
    }

    public class AvatarPreviewViewModelFactory implements ViewModelProvider.Factory {

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

    public class AvatarPreviewViewModel extends AndroidViewModel {

        final MutableLiveData<Media> media = new MutableLiveData<>();

        AvatarPreviewViewModel(@NonNull Application application, @Nullable Uri uri) {
            super(application);
            if (uri != null) {
                loadUri(uri);
            }
        }

        @Override
        protected void onCleared() {
            final Media tmpMedia = viewModel.getMedia();
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

        void preparePost() {
            new Prepare(getApplication(), getMedia()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
