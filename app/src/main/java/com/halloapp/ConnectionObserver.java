package com.halloapp;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.Contacts;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;
    private final PostsDb postsDb;

    ConnectionObserver(@NonNull Context context, @NonNull PostsDb postsDb) {
        this.context = context.getApplicationContext();
        this.postsDb = postsDb;
    }

    @Override
    public void onConnected() {
        Connection.getInstance(this).syncPubSub(Contacts.getInstance().getMemberJids());
        Contacts.getInstance().startContactSync(context);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLoginFailed() {
        HalloApp.instance.resetRegistration();
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
