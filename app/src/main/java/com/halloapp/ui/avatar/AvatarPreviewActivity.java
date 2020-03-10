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
import android.util.Base64;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.Uploader;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.VideoPlaybackActivity;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.CropImageView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.MediaUploadIq;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import me.relex.circleindicator.CircleIndicator;

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

        final View setButton = findViewById(R.id.set_avatar);
        setButton.setOnClickListener(v -> {
            viewModel.preparePost();
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

        final MediaViewPager viewPager = findViewById(R.id.media_pager);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
        viewPager.setVisibility(View.GONE);
        final CircleIndicator mediaPagerIndicator = findViewById(R.id.media_pager_indicator);
        mediaPagerIndicator.setVisibility(View.GONE);

        viewModel = new ViewModelProvider(this,
                new PostComposerViewModelFactory(getApplication(), uris)).get(PostComposerViewModel.class);
        viewModel.media.observe(this, media -> {
            progressView.setVisibility(View.GONE);
            if (!media.isEmpty()) {
                viewPager.setMaxAspectRatio(1);
                viewPager.setAdapter(new PostMediaPagerAdapter(media));
                viewPager.setVisibility(View.VISIBLE);
            }
            if (media.size() > 1) {
                mediaPagerIndicator.setVisibility(View.VISIBLE);
                mediaPagerIndicator.setViewPager(viewPager);
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
    public void finish() {
        Preconditions.checkNotNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0));
        setTitle("");
        getWindow().setStatusBarColor(0);
        super.finish();
    }

    private class PostMediaPagerAdapter extends PagerAdapter {

        final List<Media> media = new ArrayList<>();

        PostMediaPagerAdapter(@NonNull List<Media> media) {
            this.media.clear();
            this.media.addAll(media);
        }

        @Override
        public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view = getLayoutInflater().inflate(R.layout.post_composer_media_pager_item, container, false);
            final CropImageView imageView = view.findViewById(R.id.image);
            final View playButton = view.findViewById(R.id.play);
            final Media mediaItem = media.get(position);
            if (mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
                imageView.setSinglePointerDragStartDisabled(media.size() > 1);
                imageView.setReturnToMinScaleOnUp(false);
                if (mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                imageView.setOnCropListener(rect -> viewModel.cropRects.put(mediaItem.id, rect));
                imageView.setGridEnabled(true);
            } else {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setGridEnabled(false);
            }
            mediaThumbnailLoader.load(imageView, mediaItem);
            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                playButton.setVisibility(View.VISIBLE);
                playButton.setOnClickListener(v -> {
                    final Intent intent = new Intent(getBaseContext(), VideoPlaybackActivity.class);
                    intent.setData(Uri.fromFile(mediaItem.file));
                    startActivity(intent);
                });
            } else {
                playButton.setVisibility(View.GONE);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return media.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
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
                final File file = MediaStore.getInstance(application).getTmpFile(RandomId.create());
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

    class Prepare extends AsyncTask<Void, Void, Void> {

        private final List<Media> media;
        private final Map<String, RectF> cropRects;
        private final Application application;

        Prepare(@NonNull Application application, @Nullable List<Media> media, @Nullable Map<String, RectF> cropRects) {
            this.application = application;
            this.media = media;
            this.cropRects = cropRects;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (media != null) {
                Media img = media.get(0);
                final File pngFile = MediaStore.getInstance(application).getAvatarFile(UserId.ME.rawId());
                try {
                    TranscodeResult transcodeResult = transcodeToPng(img.file, pngFile, cropRects == null ? null : cropRects.get(img.id), 100, Constants.JPEG_QUALITY);
                    uploadAvatar(pngFile, Connection.getInstance(), transcodeResult);
                    AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), AvatarPreviewActivity.this);
                    avatarLoader.reportMyAvatarChanged();
                } catch (IOException | NoSuchAlgorithmException e) {
                    Log.e("failed to transcode image", e);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            setResult(RESULT_OK);
            finish();
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
        public TranscodeResult transcodeToPng(@NonNull File fileFrom, @NonNull File fileTo, @Nullable RectF cropRect, int maxDimension, int quality) throws IOException, NoSuchAlgorithmException {
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

                try (final FileOutputStream streamTo = new FileOutputStream(fileTo)) {
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, quality, streamTo);
                    InputStream is = new FileInputStream(fileTo);

                    // TODO(jack): Compute hash without re-reading the file
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] buf = new byte[1000];
                    int count;
                    int sum = 0;
                    while ((count = is.read(buf)) != -1) {
                        md.update(buf, 0, count);
                        sum += count;
                    }
                    fileSize = sum;
                    byte[] sha256hash = md.digest();
                    hash = StringUtils.bytesToHexString(sha256hash);
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

        final Map<String, RectF> cropRects = new HashMap<>();

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
