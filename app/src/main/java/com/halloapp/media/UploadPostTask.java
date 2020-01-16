package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.Connection;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

import java.io.IOException;

public class UploadPostTask extends AsyncTask<Void, Void, Void> {

    private final Post post;

    private final Connection connection;
    private final MediaStore mediaStore;
    private final PostsDb postsDb;

    public UploadPostTask(@NonNull Post post, Connection connection, MediaStore mediaStore, PostsDb postsDb) {
        this.post = post;
        this.connection = connection;
        this.mediaStore = mediaStore;
        this.postsDb = postsDb;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Uploader.UploadListener uploadListener = percent -> true;

        try {
            post.url = Uploader.run(mediaStore.getMediaFile(post.file), uploadListener);
            postsDb.setPostUrl(post.chatId, post.senderUserId, post.postId, post.url);
            connection.sendPost(post);
        } catch (IOException e) {
            Log.e("upload", e);
        }
        return null;
    }
}

