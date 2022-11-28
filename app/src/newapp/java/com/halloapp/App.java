package com.halloapp;

import android.app.Application;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.halloapp.AppContext;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.LanguageUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;

public class App extends Application {
    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        appContext.setApplicationContext(this);
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
