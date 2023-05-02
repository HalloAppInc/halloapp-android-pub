package com.halloapp.calling.calling;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.halloapp.AppContext;
import com.halloapp.Notifications;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;


public class CallService extends Service {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private static final String EXTRA_PEER_UID = "peer_uid";
    private static final String EXTRA_IS_INITIATOR = "is_initiator";

    public static Intent getIntent(@NonNull UserId peerUid, boolean isInitiator) {
        Intent serviceIntent = new Intent(AppContext.getInstance().get(), CallService.class);
        serviceIntent.putExtra(EXTRA_PEER_UID, peerUid.rawId());
        serviceIntent.putExtra(EXTRA_IS_INITIATOR, isInitiator);
        return serviceIntent;
    }

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
        super.onStartCommand(intent, flags, startId);
        String peerUidStr = Preconditions.checkNotNull(intent.getStringExtra(EXTRA_PEER_UID));
        boolean isInitiator = intent.getBooleanExtra(EXTRA_IS_INITIATOR, false);
        Log.i("CallService.onStartCommand peerUid: " + peerUidStr + " isInitiator: " + isInitiator);
        UserId peerUid = new UserId(peerUidStr);
        bgWorkers.execute(() -> {
            Notification notification = Notifications.getInstance(getApplicationContext()).getOngoingCallNotification(peerUid, isInitiator);
            startForeground(Notifications.ONGOING_CALL_NOTIFICATION_ID, notification);
        });

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

