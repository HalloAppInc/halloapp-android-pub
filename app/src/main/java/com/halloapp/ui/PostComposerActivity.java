package com.halloapp.ui;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Size;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaStore;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.PostEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class PostComposerActivity extends AppCompatActivity {

    private PostComposerViewModel viewModel;
    private MediaThumbnailLoader mediaThumbnailLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PostComposerActivity: onCreate");
        setContentView(R.layout.activity_post_composer);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mediaThumbnailLoader = new MediaThumbnailLoader(this);

        final PostEditText editText = findViewById(R.id.entry);

        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final String postText = StringUtils.preparePostText(Preconditions.checkNotNull(editText.getText()).toString());
            if (TextUtils.isEmpty(postText) && viewModel.getMedia() == null) {
                Log.w("PostComposerActivity: cannot post empty");
                return;
            }
            viewModel.preparePost(postText.trim(), viewModel.getMedia());
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

        final ViewPager viewPager = findViewById(R.id.media_pager);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
        viewPager.setVisibility(View.GONE);
        final CircleIndicator mediaPagerIndicator = findViewById(R.id.media_pager_indicator);
        mediaPagerIndicator.setVisibility(View.GONE);

        viewModel = new ViewModelProvider(this,
                new PostComposerViewModelFactory(getApplication(), uris)).get(PostComposerViewModel.class);
        viewModel.media.observe(this, media -> {
            progressView.setVisibility(View.GONE);
            if (!media.isEmpty()) {
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

    private class PostMediaPagerAdapter extends PagerAdapter {

        final List<Media> media = new ArrayList<>();

        PostMediaPagerAdapter(@NonNull List<Media> media) {
            this.media.clear();
            this.media.addAll(media);
        }

        @Override
        public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view = getLayoutInflater().inflate(R.layout.media_pager_item, container, false);
            final ImageView imageView = view.findViewById(R.id.image);
            mediaThumbnailLoader.load(imageView, media.get(position));
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
            for (Uri uri : uris) {
                final File file = MediaStore.getInstance(application).getTmpFile(RandomId.create() + ".jpg");
                FileUtils.uriToFile(application, uri, file);
                final Size size = MediaUtils.getDimensions(file);
                if (size.getHeight() > 0 && size.getWidth() > 0) {
                    int orientation = ExifInterface.ORIENTATION_UNDEFINED;
                    try {
                        orientation = MediaUtils.getExifOrientation(file);
                    } catch (IOException ignore) {
                    }
                    final Media mediaItem = Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file);
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        mediaItem.width = size.getHeight();
                        mediaItem.height = size.getWidth();
                    } else {
                        mediaItem.width = size.getWidth();
                        mediaItem.height = size.getHeight();
                    }
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
        private final Application application;
        private final MutableLiveData<Post> post;

        PreparePostTask(@NonNull Application application, @Nullable String text, @Nullable List<Media> media, @NonNull MutableLiveData<Post> post) {
            this.application = application;
            this.text = text;
            this.media = media;
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
                    text);
            if (media != null) {
                for (Media media : media) {
                    try {
                        final File postFile = MediaStore.getInstance(application).getMediaFile(RandomId.create() + ".jpg");
                        MediaUtils.transcode(media.file, postFile, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY);
                        post.media.add(Media.createFromFile(media.type, postFile));
                    } catch (IOException e) {
                        Log.e("failed to transcode image", e);
                        return null;
                    }
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

        void preparePost(@Nullable String text, @Nullable List<Media> media) {
            new PreparePostTask(getApplication(), text, media, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}
