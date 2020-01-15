package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;

    ConnectionObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnected() {
        if (HalloApp.instance.getLastSyncTime() > 0) { // initial sync done in InitialSyncActivity
            ContactsSync.getInstance(context).startPubSubSync();
        }
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
        PostsDb.getInstance(context).setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_SENT);
    }

    @Override
    public void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId) {
        PostsDb.getInstance(context).setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_DELIVERED);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        if (post.type == Post.POST_TYPE_TEXT) {
            post.state = Post.POST_STATE_INCOMING_RECEIVED;
        }
        PostsDb.getInstance(context).addPost(post);
    }

    @Override
    public void onSubscribersChanged() {
        ContactsSync.getInstance(context).startContactSync();
    }

}
