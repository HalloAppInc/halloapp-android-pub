package com.halloapp.calling;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.halloapp.Notifications;
import com.halloapp.util.logs.Log;

public class CallService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CallService.onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("CallService.onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);
        Log.i("CallService.onStartCommand");
        Notification notification = Notifications.getInstance(getApplicationContext()).getOngoingCallNotification();
        startForeground(Notifications.ONGOING_CALL_NOTIFICATION_ID, notification);
        return res;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

