package com.halloapp;

import android.text.TextUtils;

import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.Connection;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.HmsMessaging;
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
        updateHuaweiPushTokenIfNeeded();
    }

    public static void updateHuaweiPushTokenIfNeeded() {
        BgWorkers.getInstance().execute(() -> {
            if (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(AppContext.getInstance().get()) != ConnectionResult.SUCCESS) {
                Log.i("halloapp: huawei api not available");
                return;
            }
            try {
                String pushToken = HmsInstanceId.getInstance(AppContext.getInstance().get()).getToken(Constants.HUAWEI_APP_ID, HmsMessaging.DEFAULT_TOKEN_SCOPE);
                if (TextUtils.isEmpty(pushToken)) {
                    Log.e("halloapp: error getting huawei push token");
                } else {
                    Log.d("halloapp: obtained the huawei push token");

                    String locale = LanguageUtils.getLocaleIdentifier();

                    String savedLocale = Preferences.getInstance().getLastDeviceLocale();
                    String savedToken = Preferences.getInstance().getLastHuaweiPushToken();
                    long lastUpdateTime = Preferences.getInstance().getLastHuaweiPushTokenSyncTime();
                    if (!Preconditions.checkNotNull(pushToken).equals(savedToken)
                            || !locale.equals(savedLocale)
                            || System.currentTimeMillis() - lastUpdateTime > Constants.PUSH_TOKEN_RESYNC_TIME) {
                        Connection.getInstance().sendHuaweiPushToken(pushToken, locale);
                    } else {
                        Log.i("halloapp: no need to sync huawei push token");
                    }
                }
            } catch (ApiException e) {
                Log.e("halloapp: error getting huawei push token", e);
            }
        });
    }
}
