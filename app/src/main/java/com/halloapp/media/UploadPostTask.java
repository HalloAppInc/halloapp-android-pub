package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.Connection;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.protocol.MediaUploadIq;
import com.halloapp.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UploadPostTask extends AsyncTask<Void, Void, Void> {

    private final Post post;

    private final Connection connection;
    private final PostsDb postsDb;

    public UploadPostTask(@NonNull Post post, Connection connection, PostsDb postsDb) {
        this.post = post;
        this.connection = connection;
        this.postsDb = postsDb;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (Media media : post.media) {
            if (media.transferred) {
                continue;
            }
            final MediaUploadIq.Urls urls;
            try {
                urls = connection.requestMediaUpload().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("UploadPostTask", e);
                return null;
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            try {
                Uploader.run(media.file, urls.putUrl, uploadListener);
                media.url = urls.getUrl;
                media.transferred = true;
                postsDb.setMediaTransferred(post, media);
            } catch (IOException e) {
                Log.e("UploadPostTask", e);
                return null;
            }
        }
        connection.sendPost(post);
        return null;
    }
}

