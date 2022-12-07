package com.halloapp.katchup;

import android.content.Context;

import androidx.annotation.NonNull;

import com.halloapp.xmpp.Connection;

// Preliminary class pending further discussion with Clark about whether MainConnectionObserver should be reused.
// We also could move MainConnectionObserver to the halloapp flavor and rename this one to the same.
public class KatchupConnectionObserver extends Connection.Observer {

    private static KatchupConnectionObserver instance;

    public static KatchupConnectionObserver getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (KatchupConnectionObserver.class) {
                if (instance == null) {
                    instance = new KatchupConnectionObserver(context);
                }
            }
        }
        return instance;
    }

    private final Context context;

    private KatchupConnectionObserver(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public void onConnected() {
        RelationshipSyncWorker.schedule(context);
    }
}
