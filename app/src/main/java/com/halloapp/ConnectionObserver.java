package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
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
    public void onOutgoingPostAcked(@NonNull String chatId, @NonNull String postId) {
        PostsDb.getInstance(context).setPostTransferred(chatId, UserId.ME, postId);
    }

    @Override
    public void onOutgoingPostDelivered(@NonNull String chatId, @NonNull String postId) {
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        PostsDb.getInstance(context).addPost(post);
    }

    @Override
    public void onSubscribersChanged() {
        ContactsSync.getInstance(context).startContactSync();
    }

}
