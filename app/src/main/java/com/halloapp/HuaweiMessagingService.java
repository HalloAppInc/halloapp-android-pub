package com.halloapp;

import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class HuaweiMessagingService extends HmsMessageService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage == null) {
            Log.w("HuaweiMessagingService: Received empty message");
            return;
        }

        Log.d("HuaweiMessagingService: PushId: " + remoteMessage.getMessageId() + " From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().length() > 0) {
            Log.d("HuaweiMessagingService: PushId: " + remoteMessage.getMessageId() + " Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("HuaweiMessagingService: Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        Preferences.getInstance().setPendingOfflineQueue(true);

        PushSyncWorker.schedule(getApplicationContext());

        PushReceived pushReceived = PushReceived.newBuilder().setClientTimestamp(System.currentTimeMillis()).setId(remoteMessage.getMessageId()).build();
        Events.getInstance().sendEvent(pushReceived)
                .onResponse((handler) -> Log.d("HuaweiMessagingService: push_received event sent " + remoteMessage.getMessageId()))
                .onError((handler) -> Log.w("HuaweiMessagingService: push_received event failed to send " + remoteMessage.getMessageId()));
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("HuaweiMessagingService: onDeletedMessages");
    }

    @Override
    public void onNewToken(String s) {
        HalloApp.updateHuaweiPushTokenIfNeeded();
    }
}
