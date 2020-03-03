package com.halloapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.TransferPendingItemsTask;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConnectionObserver implements Connection.Observer {

    private final Context context;

    ConnectionObserver(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnected() {
        if (Preferences.getInstance(context).getLastSyncTime() > 0) { // initial sync done in InitialSyncActivity
            new TransferPendingItemsTask(context).execute();
        }
        HalloApp.sendPushTokenFromFirebase();
        new RequestExpirationInfoTask(Connection.getInstance(), context).execute();
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
    public void onIncomingPostsReceived(@NonNull List<Post> posts, @NonNull String ackId) {
        PostsDb.getInstance(context).addPosts(posts, () -> Connection.getInstance().sendAck(ackId));
    }

    @Override
    public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull String ackId) {
        PostsDb.getInstance(context).setOutgoingPostSeen(seenByUserId, postId, timestamp, () -> Connection.getInstance().sendAck(ackId));
    }

    @Override
    public void onOutgoingCommentSent(@NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {
        PostsDb.getInstance(context).setCommentTransferred(postSenderUserId, postId, UserId.ME, commentId);
    }

    @Override
    public void onIncomingCommentsReceived(@NonNull List<Comment> comments, @NonNull String ackId) {
        PostsDb.getInstance(context).addComments(comments, () -> Connection.getInstance().sendAck(ackId));
    }

    @Override
    public void onSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        PostsDb.getInstance(context).setSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onContactsChanged(@NonNull List<ContactInfo> protocolContacts, @NonNull String ackId) {
        final List<ContactsDb.ContactFriendship> contactsFriendship = new ArrayList<>(protocolContacts.size());
        for (ContactInfo contact : protocolContacts) {
            contactsFriendship.add(new ContactsDb.ContactFriendship(new UserId(contact.normalizedPhone), "friends".equals(contact.role)));
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                ContactsDb.getInstance(context).updateContactsFriendship(contactsFriendship).get();
                Connection.getInstance().sendAck(ackId);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectionObserver.onContactsChanged", e);
            }
        });
    }
}
