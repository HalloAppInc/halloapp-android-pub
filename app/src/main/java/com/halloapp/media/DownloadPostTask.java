package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;

import java.io.IOException;

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
        for (Media media : post.media) {
            if (media.transferred) {
                continue;
            }
            final String file = RandomId.create() + ".jpg";
            final Downloader.DownloadListener downloadListener = percent -> true;
            try {
                Downloader.run(media.url, mediaStore.getMediaFile(file), downloadListener);
                media.file = file;
                media.transferred = true;
                postsDb.setMediaTransferred(post, media);
            } catch (IOException e) {
                Log.e("upload", e);
                return null;
            }
        }
        return null;
    }
}
