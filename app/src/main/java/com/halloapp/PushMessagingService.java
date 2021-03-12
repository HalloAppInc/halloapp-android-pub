package com.halloapp;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.Connection;

public class PushMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("PushMessagingService: From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("PushMessagingService: Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("PushMessagingService: Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Connection.getInstance().connect();

        PushReceived pushReceived = PushReceived.newBuilder().setClientTimestamp(System.currentTimeMillis()).setId(remoteMessage.getMessageId()).build();
        Events.getInstance().sendEvent(pushReceived);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        HalloApp.updateFirebasePushTokenIfNeeded();
    }
}
