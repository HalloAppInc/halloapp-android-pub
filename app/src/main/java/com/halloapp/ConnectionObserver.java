package com.halloapp;

import androidx.annotation.NonNull;

import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

public class ConnectionObserver implements Connection.Observer {

    final PostsDb postsDb;

    ConnectionObserver(PostsDb postsDb) {
        this.postsDb = postsDb;
    }

    @Override
    public void onOutgoingPostAcked(@NonNull String chatJid, @NonNull String postId) {
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_SENT);
    }

    @Override
    public void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId) {
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_DELIVERED);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        postsDb.addPost(post);
    }
}
