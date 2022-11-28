package com.halloapp;

import android.text.TextUtils;

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

        // The ID returned by remoteMessage.getMessageId() does not match the ID Huawei returned to the server, so we are implementing our own IDs
        String ourId = remoteMessage.getDataOfMap().get("push_id");
        String pushId = TextUtils.isEmpty(ourId) ? remoteMessage.getMessageId() : ourId;

        Log.d("HuaweiMessagingService: PushId: " + pushId + " From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().length() > 0) {
            Log.d("HuaweiMessagingService: PushId: " + pushId + " Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("HuaweiMessagingService: Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        Preferences.getInstance().setPendingOfflineQueue(true);

        PushSyncWorker.schedule(getApplicationContext());

        PushReceived pushReceived = PushReceived.newBuilder().setClientTimestamp(System.currentTimeMillis()).setId(pushId).build();
        Events.getInstance().sendEvent(pushReceived)
                .onResponse((handler) -> Log.d("HuaweiMessagingService: push_received event sent " + pushId))
                .onError((handler) -> Log.w("HuaweiMessagingService: push_received event failed to send " + pushId));
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("HuaweiMessagingService: onDeletedMessages");
    }

    @Override
    public void onNewToken(String s) {
        App.updateHuaweiPushTokenIfNeeded();
    }
}
