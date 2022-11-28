package com.halloapp;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;

public class PushMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("PushMessagingService: PushId: " + remoteMessage.getMessageId() + " From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("PushMessagingService: PushId: " + remoteMessage.getMessageId() + " Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("PushMessagingService: Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        Preferences.getInstance().setPendingOfflineQueue(true);

        PushSyncWorker.schedule(getApplicationContext());

        String nullSafeId = remoteMessage.getMessageId() == null ? "null" : remoteMessage.getMessageId();
        PushReceived pushReceived = PushReceived.newBuilder().setClientTimestamp(System.currentTimeMillis()).setId(nullSafeId).build();
        Events.getInstance().sendEvent(pushReceived)
                .onResponse((handler) -> Log.d("PushMessagingService: push_received event sent " + remoteMessage.getMessageId()))
                .onError((handler) -> Log.w("PushMessagingService: push_received event failed to send " + remoteMessage.getMessageId()));
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("PushMessagingService: onDeletedMessages");
    }

    @Override
    public void onNewToken(@NonNull String s) {
        App.updateFirebasePushTokenIfNeeded();
    }
}
