package com.halloapp;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.Connection;

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
        updateFirebasePushTokenIfNeeded();
    }

    public static void updateFirebasePushTokenIfNeeded() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e( "halloapp: getInstanceId failed", task.getException());
                        return;
                    }
                    // Get the Instance ID token.
                    final String pushToken = task.getResult();
                    if (TextUtils.isEmpty(pushToken)) {
                        Log.e("halloapp: error getting push token");
                    } else {
                        Log.d("halloapp: obtained the push token!");

                        String locale = LanguageUtils.getLocaleIdentifier();

                        String savedLocale = Preferences.getInstance().getLastDeviceLocale();
                        String savedToken = Preferences.getInstance().getLastPushToken();
                        long lastUpdateTime = Preferences.getInstance().getLastPushTokenSyncTime();
                        if (!Preconditions.checkNotNull(pushToken).equals(savedToken)
                                || !locale.equals(savedLocale)
                                || System.currentTimeMillis() - lastUpdateTime > Constants.PUSH_TOKEN_RESYNC_TIME) {
                            Connection.getInstance().sendPushToken(pushToken, locale);
                        } else {
                            Log.i("halloapp: no need to sync push token");
                        }
                    }
                });
    }
}
