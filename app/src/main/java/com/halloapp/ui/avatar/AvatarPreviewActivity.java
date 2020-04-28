package com.halloapp.ui.avatar;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Size;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
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
import com.halloapp.media.Uploader;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.CropImageView;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.MediaUploadIq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AvatarPreviewActivity extends AppCompatActivity {

    private PostComposerViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;

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
        setButton.setOnClickListener(v -> {
            viewModel.preparePost();
        });

        final View resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(v -> {
            final CropImageView imageView = findViewById(R.id.image);
            imageView.getAttacher().update();
        });

        final View progressView = findViewById(R.id.progress);

        final ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            progressView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                CenterToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            setButton.setEnabled(false);
        }

        viewModel = new ViewModelProvider(this,
                new PostComposerViewModelFactory(getApplication(), uris)).get(PostComposerViewModel.class);
        viewModel.media.observe(this, media -> {
            progressView.setVisibility(View.GONE);
            if (!media.isEmpty()) {
                final CropImageView imageView = findViewById(R.id.image);
                final Media mediaItem = media.get(0);
                imageView.setSinglePointerDragStartDisabled(media.size() > 1);
                imageView.setReturnToMinScaleOnUp(false);
                imageView.setOnCropListener(rect -> viewModel.cropRects.put(mediaItem.file, rect));
                imageView.setGridEnabled(false);
                mediaThumbnailLoader.load(imageView, mediaItem);
                imageView.setVisibility(View.VISIBLE);
            }
            if (media.size() > 1) {
                throw new IllegalStateException("Can only have a single profile photo"); // TODO(jack)
            }
            if (uris != null && media.size() != uris.size()) {
                CenterToast.show(getBaseContext(), R.string.failed_to_load_media);
            }
            setButton.setEnabled(true);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AvatarPreviewActivity: onDestroy");
        mediaThumbnailLoader.destroy();
        final List<Media> tmpMedia = viewModel.getMedia();
        if (tmpMedia != null) {
            new CleanupTmpFilesTask(tmpMedia).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.profile_photo_crop_menu, menu);
        return true;
    }

    @Override
    public void finish() {
        Preconditions.checkNotNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0));
        setTitle("");
        getWindow().setStatusBarColor(0);
        super.finish();
    }

    static class LoadPostUrisTask extends AsyncTask<Void, Void, List<Media>> {

        private final Collection<Uri> uris;
        private final Application application;
        private final MutableLiveData<List<Media>> media;

        LoadPostUrisTask(@NonNull Application application, @NonNull Collection<Uri> uris, @NonNull MutableLiveData<List<Media>> media) {
            this.application = application;
            this.uris = uris;
            this.media = media;
        }

        @Override
        protected List<Media> doInBackground(Void... voids) {
            final List<Media> media = new ArrayList<>();
            final ContentResolver contentResolver = application.getContentResolver();
            for (Uri uri : uris) {
                @Media.MediaType int mediaType = Media.getMediaType(contentResolver.getType(uri));
                final File file = FileStore.getInstance(application).getTmpFile(RandomId.create());
                FileUtils.uriToFile(application, uri, file);
                final Size size = MediaUtils.getDimensions(file, mediaType);
                if (size != null) {
                    final Media mediaItem = Media.createFromFile(mediaType, file);
                    mediaItem.width = size.getWidth();
                    mediaItem.height = size.getHeight();
                    media.add(mediaItem);
                } else {
                    Log.e("AvatarPreviewActivity: failed to load " + uri);
                }
            }
            return media;
        }

        @Override
        protected void onPostExecute(List<Media> media) {
            this.media.postValue(media);
        }
    }

    static class CleanupTmpFilesTask extends AsyncTask<Void, Void, Void> {

        private final List<Media> media;

        CleanupTmpFilesTask(@NonNull List<Media> media) {
            this.media = media;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Media mediaItem : media) {
                if (!mediaItem.file.delete()) {
                    Log.e("failed to delete temporary file " + mediaItem.file.getAbsolutePath());
                }
            }
            return null;
        }
    }

    class Prepare extends AsyncTask<Void, Void, Boolean> {

        private final List<Media> media;
        private final Map<File, RectF> cropRects;
        private final Application application;

        Prepare(@NonNull Application application, @Nullable List<Media> media, @Nullable Map<File, RectF> cropRects) {
            this.application = application;
            this.media = media;
            this.cropRects = cropRects;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (media != null) {
                Media img = media.get(0);
                final File outFile = FileStore.getInstance(application).getAvatarFile(UserId.ME.rawId());
                try {
                    TranscodeResult transcodeResult = transcode(img.file, outFile, cropRects == null ? null : cropRects.get(img.file), Constants.MAX_AVATAR_DIMENSION);
                    uploadAvatar(outFile, Connection.getInstance(), transcodeResult);
                    AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), AvatarPreviewActivity.this);
                    avatarLoader.reportMyAvatarChanged();
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
            if (bitmap != null) {
                final Bitmap croppedBitmap;
                if (cropRect != null) {
                    final Rect bitmapRect = new Rect((int)(bitmap.getWidth() * cropRect.left), (int)(bitmap.getHeight() * cropRect.top),
                            (int)(bitmap.getWidth() * cropRect.right), (int)(bitmap.getHeight() * cropRect.bottom));
                    croppedBitmap = Bitmap.createBitmap(bitmapRect.width(), bitmapRect.height(), Bitmap.Config.ARGB_8888);
                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawBitmap(bitmap, bitmapRect, new Rect(0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight()), null);
                    bitmap.recycle();
                } else {
                    croppedBitmap = bitmap;
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

        public void uploadAvatar(File pngFile, Connection connection, final TranscodeResult transcodeResult) {
            final MediaUploadIq.Urls urls;
            try {
                urls = connection.requestMediaUpload().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("upload avatar", e);
                return;
            }
            if (urls == null) {
                Log.e("upload avatar: failed to get urls");
                return;
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            try {
                Uploader.run(pngFile, null, Media.MEDIA_TYPE_UNKNOWN, urls.putUrl, uploadListener);
                connection.publishAvatarMetadata(transcodeResult.hash, urls.getUrl, transcodeResult.byteCount, transcodeResult.height, transcodeResult.width);
            } catch (IOException e) {
                Log.e("upload avatar", e);
                return;
            }
        }
    }

    public class PostComposerViewModelFactory implements ViewModelProvider.Factory {

        private final Application application;
        private final Collection<Uri> uris;


        PostComposerViewModelFactory(@NonNull Application application, @Nullable Collection<Uri> uris) {
            this.application = application;
            this.uris = uris;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new PostComposerViewModel(application, uris);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public class PostComposerViewModel extends AndroidViewModel {

        final MutableLiveData<List<Media>> media = new MutableLiveData<>();

        final Map<File, RectF> cropRects = new HashMap<>();

        PostComposerViewModel(@NonNull Application application, @Nullable Collection<Uri> uris) {
            super(application);
            if (uris != null) {
                loadUris(uris);
            }
        }

        List<Media> getMedia() {
            return media.getValue();
        }

        private void loadUris(@NonNull Collection<Uri> uris) {
            new LoadPostUrisTask(getApplication(), uris, media).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        void preparePost() {
            new Prepare(getApplication(), getMedia(), cropRects).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
