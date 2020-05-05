package com.halloapp.xmpp;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;
import com.halloapp.util.ViewDataLoader;

import java.util.HashMap;
import java.util.Map;

public class PresenceLoader extends ViewDataLoader<View, Long, String> {

    private static PresenceLoader instance;

    private final Connection connection;
    private final Map<UserId, MutableLiveData<Long>> map = new HashMap<>();

    public static PresenceLoader getInstance(Connection connection) {
        if (instance == null) {
            synchronized (PresenceLoader.class) {
                if (instance == null) {
                    instance = new PresenceLoader(connection);
                }
            }
        }
        return instance;
    }

    private PresenceLoader(Connection connection) {
        this.connection = connection;
    }

    public LiveData<Long> getLastSeenLiveData(UserId userId) {
        MutableLiveData<Long> mld = map.get(userId);
        if (mld == null) {
            mld = new MutableLiveData<>();
            map.put(userId, mld);
            connection.subscribePresence(userId);
        }
        return mld;
    }

    public void reportPresence(UserId userId, Long lastSeen) {
        MutableLiveData<Long> mld = map.get(userId);
        if (mld == null) {
            Log.w("Received unexpected presence for user " + userId);
            return;
        }
        mld.postValue(lastSeen);
    }
}
