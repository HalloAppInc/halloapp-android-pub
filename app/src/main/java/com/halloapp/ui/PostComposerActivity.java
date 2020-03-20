package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Size;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.CropImageView;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.PostEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class PostComposerActivity extends AppCompatActivity {

    private PostComposerViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PostComposerActivity: onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_post_composer);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));

        final PostEditText editText = findViewById(R.id.entry);

        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final String postText = StringUtils.preparePostText(Preconditions.checkNotNull(editText.getText()).toString());
            if (TextUtils.isEmpty(postText) && viewModel.getMedia() == null) {
                Log.w("PostComposerActivity: cannot post empty");
                return;
            }
            viewModel.preparePost(postText.trim());
        });

        final View progressView = findViewById(R.id.progress);

        final ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            progressView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                CenterToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            sendButton.setEnabled(false);
            editText.setHint(R.string.type_a_caption_hint);
        } else {
            progressView.setVisibility(View.GONE);
            editText.requestFocus();
            editText.setHint(R.string.type_a_post_hint);
            editText.setPreImeListener((keyCode, event) -> {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                    return true;
                }
                return false;
            });
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
                viewPager.setMaxAspectRatio(Constants.MAX_IMAGE_ASPECT_RATIO/*Math.min(Constants.MAX_IMAGE_ASPECT_RATIO, Media.getMaxAspectRatio(media))*/);
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
            sendButton.setEnabled(true);
        });
        viewModel.post.observe(this, post -> {
            if (post != null) {
                PostsDb.getInstance(Preconditions.checkNotNull(getBaseContext())).addPost(post);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PostComposerActivity: onDestroy");
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
                imageView.setOnCropListener(rect -> viewModel.cropRects.put(mediaItem.file, rect));
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
                final File file = FileStore.getInstance(application).getTmpFile(RandomId.create());
                FileUtils.uriToFile(application, uri, file);
                final Size size = MediaUtils.getDimensions(file, mediaType);
                if (size != null) {
                    final Media mediaItem = Media.createFromFile(mediaType, file);
                    mediaItem.width = size.getWidth();
                    mediaItem.height = size.getHeight();
                    media.add(mediaItem);
                } else {
                    Log.e("PostComposerActivity: failed to load " + uri);
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

    static class PreparePostTask extends AsyncTask<Void, Void, Post> {

        private final String text;
        private final List<Media> media;
        private final Map<File, RectF> cropRects;
        private final Application application;
        private final MutableLiveData<Post> post;

        PreparePostTask(@NonNull Application application, @Nullable String text, @Nullable List<Media> media, @Nullable Map<File, RectF> cropRects, @NonNull MutableLiveData<Post> post) {
            this.application = application;
            this.text = text;
            this.media = media;
            this.cropRects = cropRects;
            this.post = post;
        }

        @Override
        protected Post doInBackground(Void... voids) {

            final Post post = new Post(
                    0,
                    UserId.ME,
                    RandomId.create(),
                    System.currentTimeMillis(),
                    false,
                    Post.POST_SEEN_YES,
                    text);
            if (media != null) {
                for (Media media : media) {
                    final File postFile = FileStore.getInstance(application).getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                    switch (media.type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            try {
                                MediaUtils.transcodeImage(media.file, postFile, cropRects == null ? null : cropRects.get(media.file), Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY);
                            } catch (IOException e) {
                                Log.e("failed to transcode image", e);
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            if (!media.file.renameTo(postFile)) {
                                Log.e("failed to rename " + media.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            Log.e("unknown media type " + media.file.getAbsolutePath());
                            return null;
                        }
                    }
                    final Media sendMedia = Media.createFromFile(media.type, postFile);
                    post.media.add(sendMedia);
                }
            }
            return post;
        }

        @Override
        protected void onPostExecute(Post post) {
            this.post.postValue(post);
        }
    }

    public static class PostComposerViewModelFactory implements ViewModelProvider.Factory {

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

    public static class PostComposerViewModel extends AndroidViewModel {

        final MutableLiveData<List<Media>> media = new MutableLiveData<>();
        final MutableLiveData<Post> post = new MutableLiveData<>();

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

        void preparePost(@Nullable String text) {
            new PreparePostTask(getApplication(), text, getMedia(), cropRects, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
