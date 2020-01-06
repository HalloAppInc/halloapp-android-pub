package com.halloapp;

import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.media.Downloader;
import com.halloapp.media.Uploader;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainPostsObserver implements PostsDb.Observer {

    private final Connection connection;
    private final File filesDir;
    private final PostsDb postsDb;

    MainPostsObserver(@NonNull Connection connection, @NonNull File filesDir, @NonNull PostsDb postsDb) {
        this.connection = connection;
        this.filesDir = filesDir;
        this.postsDb = postsDb;
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.isOutgoing()) {
            if (post.type == Post.POST_TYPE_TEXT) {
                connection.sendPost(post);
            } else {
                postsDb.setPostState(post.chatJid, post.senderJid, post.postId, Post.POST_STATE_OUTGOING_UPLOADING);
                Uploader.UploadListener uploadListener = new Uploader.UploadListener() {
                    @Override
                    public boolean onProgress(int percent) {
                        return true;
                    }
                };
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            post.url = Uploader.run(new File(filesDir, post.file), uploadListener);
                            postsDb.setPostUrl(post.chatJid, post.senderJid, post.postId, post.url);
                            //postsDb.setPostState(post.chatJid, post.senderJid, post.postId, Post.POST_STATE_OUTGOING_SENDING);
                            connection.sendPost(post);
                        } catch (IOException e) {
                            Log.e("upload", e);
                        }
                        return null;
                    }
                }.execute();
            }
        } else { // if (post.isIncoming())
            connection.sendDeliveryReceipt(post);

            if (!TextUtils.isEmpty(post.url)) {
                postsDb.setPostState(post.chatJid, post.senderJid, post.postId, Post.POST_STATE_INCOMING_DOWNLOADING);
                Downloader.DownloadListener downloadListener = new Downloader.DownloadListener() {
                    @Override
                    public boolean onProgress(int percent) {
                        return true;
                    }
                };
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            final String file = UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
                            Downloader.run(post.url, new File(filesDir, file), downloadListener);
                            postsDb.setPostFile(post.chatJid, post.senderJid, post.postId, file);
                        } catch (IOException e) {
                            Log.e("upload", e);
                        }
                        return null;
                    }
                }.execute();
            }
        }
    }

    @Override
    public void onPostDuplicate(@NonNull Post post) {
    }

    @Override
    public void onPostDeleted(@NonNull Post post) {
    }

    @Override
    public void onPostStateChanged(@NonNull String chatJid, @NonNull String senderJid, @NonNull String postId, int state) {
    }

    @Override
    public void onPostMediaUpdated(@NonNull String chatJid, @NonNull String senderJid, @NonNull String postId) {
    }

}
