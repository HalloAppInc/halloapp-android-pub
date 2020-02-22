package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.TransferPendingItemsTask;
import com.halloapp.xmpp.Connection;

import java.util.Collection;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;

    ConnectionObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnected() {
        if (Preferences.getInstance(context).getLastSyncTime() > 0) { // initial sync done in InitialSyncActivity
            ContactsSync.getInstance(context).startPubSubSync();
            new TransferPendingItemsTask(context).execute();
        }
        HalloApp.instance.sendPushTokenFromFirebase();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLoginFailed() {
        Me.getInstance(context).resetRegistration();
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId) {
        PostsDb.getInstance(context).setPostTransferred(UserId.ME, postId);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        PostsDb.getInstance(context).addPost(post);
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp) {
        PostsDb.getInstance(context).setOutgoingPostSeen(seenByUserId, postId, timestamp);
    }

    @Override
    public void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        PostsDb.getInstance(context).setCommentTransferred(postSenderUserId, postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingCommentReceived(@NonNull Comment comment) {
        PostsDb.getInstance(context).addComment(comment);
    }

    @Override
    public void onSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        PostsDb.getInstance(context).setSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onFeedHistoryReceived(@NonNull Collection<Post> historyPosts, @NonNull Collection<Comment> historyComments) {
        PostsDb.getInstance(context).addHistory(historyPosts, historyComments);
    }

    @Override
    public void onSubscribersChanged() {
        ContactsSync.getInstance(context).startContactSync();
    }
}
