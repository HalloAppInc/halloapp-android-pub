package com.halloapp;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.ChatMessage;
import com.halloapp.posts.Comment;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.posts.TransferPendingItemsTask;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.RegistrationRequestActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ContactInfo;
import com.halloapp.xmpp.PublishedAvatarMetadata;

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
        new TransferPendingItemsTask(context).execute();
        HalloApp.sendPushTokenFromFirebase();
        new RequestExpirationInfoTask(Connection.getInstance(), context).execute();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLoginFailed() {
        Me.getInstance(context).resetRegistration();
        if (ForegroundObserver.getInstance().isInForeground()) {
            RegistrationRequestActivity.reVerify(context);
        } else {
            Notifications.getInstance(context).showLoginFailedNotification();
        }
    }

    @Override
    public void onClientVersionExpired() {
        if (ForegroundObserver.getInstance().isInForeground()) {
            AppExpirationActivity.open(context, 0);
        } else {
            Notifications.getInstance(context).showExpirationNotification(0);
        }
    }

    @Override
    public void onOutgoingPostSent(@NonNull String postId) {
        PostsDb.getInstance(context).setPostTransferred(UserId.ME, postId);
    }

    @Override
    public void onIncomingFeedItemsReceived(@NonNull List<Post> posts, @NonNull List<Comment> comments, @NonNull String ackId) {
        PostsDb.getInstance(context).addFeedItems(posts, comments, () -> Connection.getInstance().sendAck(ackId));
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
    public void onSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        PostsDb.getInstance(context).setSeenReceiptSent(senderUserId, postId);
    }

    @Override
    public void onOutgoingMessageSent(@NonNull String chatId, @NonNull String messageId) {
        // TODO (ds): implement
        Log.i("ConnectionObserver.onOutgoingMessageSent chatId=" + chatId + " messageId=" + messageId);
    }

    @Override
    public void onIncomingMessageReceived(@NonNull ChatMessage message) {
        // TODO (ds): implement
        Log.i("ConnectionObserver.onIncomingMessageReceived chatId=" + message.chatId + " messageId=" + message.messageId);
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

    @Override
    public void onAvatarMetadatasReceived(@NonNull UserId metadataUserId, @NonNull List<PublishedAvatarMetadata> pams, @NonNull String ackId) {
        AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), context);
        for (PublishedAvatarMetadata pam : pams) {
            avatarLoader.reportAvatarMetadataUpdate(metadataUserId, pam.getHash(), pam.getUrl());
        }
        Connection.getInstance().sendAck(ackId);
    }
}
