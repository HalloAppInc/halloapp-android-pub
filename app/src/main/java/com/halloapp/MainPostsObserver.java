package com.halloapp;

import androidx.annotation.NonNull;

import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

public class MainPostsObserver implements PostsDb.Observer {

    private final Connection connection;

    MainPostsObserver(@NonNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onPostAdded(@NonNull Post post) {
        if (post.isOutgoing()) {
            connection.sendPost(post);
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
}
