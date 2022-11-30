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
        Log.init(FileStore.getInstance());
        Log.i("HalloApp init " + BuildConfig.VERSION_NAME + " " + BuildConfig.GIT_HASH);
    }
}
