package com.halloapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.halloapp.Connection;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PostComposerActivity extends AppCompatActivity {

    private File postFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_composer);

        final EditText editText = findViewById(R.id.entry);

        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final String postText = editText.getText().toString();
            if (postText.trim().isEmpty() && postFile == null) {
                Log.w("PostComposerActivity: cannot post empty");
                return;
            }
            final Post post = new Post(
                    0,
                    Connection.FEED_JID.toString(),
                    "",
                    UUID.randomUUID().toString().replaceAll("-", ""),
                    "",
                    0,
                    System.currentTimeMillis(),
                    Post.POST_STATE_OUTGOING_SENDING,
                    postFile == null ? Post.POST_TYPE_TEXT : Post.POST_TYPE_IMAGE,
                    postText,
                    null,
                    postFile == null ? null : postFile.getName());
            PostsDb.getInstance(Preconditions.checkNotNull(getBaseContext())).addPost(post);
            finish();
        });

        final Uri uri = getIntent().getData();
        if (uri != null) {
            final File file = new File(getFilesDir(), UUID.randomUUID().toString().replace("-", "") + ".jpg");
            sendButton.setEnabled(false);

            final PostComposerViewModel model = ViewModelProviders.of(this,
                    new PostComposerViewModelFactory(getApplication(), uri, file)).get(PostComposerViewModel.class);
            model.getData().observe(this, data -> {
                final ImageView imageView = findViewById(R.id.image);
                imageView.setImageBitmap(data);
                if (data != null) {
                    postFile = file;
                }
                sendButton.setEnabled(true);
            });
        } else {
            editText.requestFocus();
        }
    }

    static class LoadPostUriTask extends AsyncTask<Void, Void, Bitmap> {

        private final Uri uri;
        private final File file;
        private final Application application;
        private final MutableLiveData<Bitmap> data;

        LoadPostUriTask(@NonNull Application application, @NonNull Uri uri, @NonNull File file, @NonNull MutableLiveData<Bitmap> data) {
            this.application = application;
            this.uri = uri;
            this.file = file;
            this.data = data;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            final File tmpFile = new File(application.getCacheDir(), "tmp.jpg");
            FileUtils.uriToFile(application, uri, tmpFile);
            final Bitmap bitmap;
            try {
                bitmap = MediaUtils.transcode(tmpFile, file, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY);
            } catch (IOException e) {
                Log.e("failed to transcode image", e);
                return null;
            } finally {
                tmpFile.delete();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            data.postValue(bitmap);
        }
    }

    public static class PostComposerViewModelFactory implements ViewModelProvider.Factory {

        private Application application;
        private Uri uri;
        private File file;


        PostComposerViewModelFactory(@NonNull Application application, @NonNull Uri uri, @NonNull File file) {
            this.application = application;
            this.uri = uri;
            this.file = file;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PostComposerViewModel(application, uri, file);
        }
    }

    public static class PostComposerViewModel extends AndroidViewModel {

        private final MutableLiveData<Bitmap> data = new MutableLiveData<>();

        PostComposerViewModel(@NonNull Application application, @NonNull Uri uri, @NonNull File file) {
            super(application);
            loadData(uri, file);
        }

        LiveData<Bitmap> getData() {
            return data;
        }

        private void loadData(@NonNull Uri uri, @NonNull File file) {
            new LoadPostUriTask(getApplication(), uri, file, data).execute();
        }
    }
}
