package com.halloapp.katchup;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ShareReceiver extends BroadcastReceiver {

    private static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName component = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
        long notificationId = intent.getLongExtra(EXTRA_NOTIFICATION_ID, 0);

        if (component != null) {
            Analytics.getInstance().externalShare(component.getPackageName(), notificationId);
        } else {
            Analytics.getInstance().externalShare("system", notificationId);
        }
    }
}
