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
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_SENT);
    }

    @Override
    public void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId) {
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_DELIVERED);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        if (post.type == Post.POST_TYPE_TEXT) {
            post.state = Post.POST_STATE_INCOMING_RECEIVED;
        }
        postsDb.addPost(post);
    }
}
