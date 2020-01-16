package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

import java.io.IOException;
import java.util.UUID;

public class DownloadPostTask extends AsyncTask<Void, Void, Void> {

    private final Post post;

    private final MediaStore mediaStore;
    private final PostsDb postsDb;

    public DownloadPostTask(@NonNull Post post, MediaStore mediaStore, PostsDb postsDb) {
        this.post = post;
        this.mediaStore = mediaStore;
        this.postsDb = postsDb;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final Downloader.DownloadListener downloadListener = percent -> true;
        final String file = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
        try {
            Downloader.run(post.url, mediaStore.getMediaFile(file), downloadListener);
            postsDb.setPostFile(post.chatJid, post.senderJid, post.postId, file);
        } catch (IOException e) {
            Log.e("upload", e);
        }
        return null;
    }
}
