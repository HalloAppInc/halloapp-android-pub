package com.halloapp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.SendPendingPostsTask;

import java.util.Collection;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;

    ConnectionObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnected() {
        if (HalloApp.instance.getLastSyncTime() > 0) { // initial sync done in InitialSyncActivity
            ContactsSync.getInstance(context).startPubSubSync();
            new SendPendingPostsTask(context).execute();
        }
        HalloApp.instance.sendPushTokenFromFirebase();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLoginFailed() {
        HalloApp.instance.resetRegistration();
    }

    @Override
    public void onOutgoingPostAcked(@NonNull String postId) {
        PostsDb.getInstance(context).setPostTransferred(UserId.ME, postId);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        PostsDb.getInstance(context).addPost(post);
        // Show push notifications if necessary.
        if (!HalloApp.instance.appActiveStatus) {
            final String title = post.senderUserId.rawId();
            final String body = post.text.isEmpty() ? "Image" : post.text;
            HalloApp.instance.showNotification(title, body);
        }
    }

    @Override
    public void onOutgoingCommentAcked(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        PostsDb.getInstance(context).setCommentTransferred(postSenderUserId, postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingCommentReceived(@NonNull Comment comment) {
        PostsDb.getInstance(context).addComment(comment);
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
