package com.halloapp.xmpp;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.ForegroundChat;
import com.halloapp.contacts.UserId;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;

import java.util.HashMap;
import java.util.Map;

public class PresenceLoader extends ViewDataLoader<View, Long, String> {

    private static PresenceLoader instance;

    private final Connection connection;
    private final Map<UserId, MutableLiveData<PresenceState>> map = new HashMap<>();

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

    public LiveData<PresenceState> getLastSeenLiveData(UserId userId) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            mld = new MutableLiveData<>();
            map.put(userId, mld);
            connection.subscribePresence(userId);
        }
        return mld;
    }

    public void reportPresence(UserId userId, Long lastSeen) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            Log.w("Received unexpected presence for user " + userId);
            return;
        }
        if (lastSeen != null) {
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_OFFLINE, lastSeen * 1000L));
        } else {
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_ONLINE));
        }
    }

    public void onDisconnect() {
        Log.d("PresenceLoader marking all as unknown");
        for (UserId userId : map.keySet()) {
            MutableLiveData<PresenceState> mld = Preconditions.checkNotNull(map.get(userId));
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_UNKNOWN));
        }
    }

    public void onReconnect() {
        Log.d("PresenceLoader resetting subscriptions");
        UserId keepUserId = null;
        MutableLiveData<PresenceState> keepMld = null;
        for (UserId userId : map.keySet()) {
            MutableLiveData<PresenceState> mld = Preconditions.checkNotNull(map.get(userId));
            if (ForegroundChat.getInstance().isForegroundChatId(userId.rawId())) {
                keepUserId = userId;
                keepMld = mld;
                break;
            }
        }
        map.clear();
        if (keepUserId != null) {
            Log.d("PresenceLoader maintaining subscription to " + keepUserId);
            map.put(keepUserId, keepMld);
            connection.subscribePresence(keepUserId);
        }
    }

    public static class PresenceState {
        public static final int PRESENCE_STATE_UNKNOWN = 0;
        public static final int PRESENCE_STATE_ONLINE = 1;
        public static final int PRESENCE_STATE_OFFLINE = 2;

        public final int state;
        public final long lastSeen;

        private PresenceState(int state) {
            this.state = state;
            this.lastSeen = 0;
        }

        private PresenceState(int state, long lastSeen) {
            this.state = state;
            this.lastSeen = lastSeen;
        }
    }
}
