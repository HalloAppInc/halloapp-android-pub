package com.halloapp.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ContactSyncAdapterService extends Service {

    private static ContactSyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        synchronized (ContactSyncAdapterService.class) {
            if (syncAdapter == null) {
                syncAdapter = new ContactSyncAdapter(this, true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
