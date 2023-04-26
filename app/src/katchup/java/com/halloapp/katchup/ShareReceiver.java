package com.halloapp.katchup;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ShareReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName component = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
        if (component != null) {
            Analytics.getInstance().externalShare(component.getPackageName());
        } else {
            Analytics.getInstance().externalShare("system");
        }
    }
}
