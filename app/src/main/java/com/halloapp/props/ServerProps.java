package com.halloapp.props;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.ConnectionObservers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerProps {

    private static final String PREFS_NAME = "props";

    private static final String PREF_KEY_APP_VERSION = "props:app_version";
    private static final String PREF_KEY_PROPS_HASH = "props:version_hash";
    private static final String PREF_KEY_LAST_PROPS_FETCH = "props:last_fetch";

    private static final long PROPS_EXPIRATION_MS = DateUtils.DAY_IN_MILLIS;

    private static final String PROP_INTERNAL_USER = "dev";
    private static final String PROP_GROUP_CHATS_ENABLED = "group_chat";
    private static final String PROP_MAX_GROUP_SIZE = "max_group_size";
    private static final String PROP_GROUP_FEED = "group_feed";
    private static final String PROP_SILENT_CHAT_MESSAGES = "silent_chat_messages";

    private static ServerProps instance;

    public static ServerProps getInstance() {
        if (instance == null) {
            synchronized (ServerProps.class) {
                if (instance == null) {
                    instance = new ServerProps(AppContext.getInstance(), Connection.getInstance(), ConnectionObservers.getInstance());
                }
            }
        }
        return instance;
    }

    private final List<Prop> serverProps = new ArrayList<>();

    private final AppContext appContext;
    private final Connection connection;

    private SharedPreferences preferences;

    private final BooleanProp propInternalUser = createProp(PROP_INTERNAL_USER, false);
    private final IntegerProp propMaxGroupSize = createProp(PROP_MAX_GROUP_SIZE, 25);
    private final BooleanProp propGroupChatsEnabled = createProp(PROP_GROUP_CHATS_ENABLED, true);
    private final BooleanProp propGroupFeedEnabled = createProp(PROP_GROUP_FEED, false);
    private final IntegerProp propSilentChatMessages = createProp(PROP_SILENT_CHAT_MESSAGES, 5);

    private final Connection.Observer connectionObserver = new Connection.Observer() {
        @Override
        public void onConnected() {
            String propHash = connection.getConnectionPropHash();
            if (propHash != null) {
                onReceiveServerPropsHash(propHash);
            }
        }

        @Override
        public void onServerPropsReceived(@NonNull Map<String, String> props, @NonNull String hash) {
            onReceiveServerProps(props, hash);
        }
    };

    private ServerProps(@NonNull AppContext appContext, @NonNull Connection connection, @NonNull ConnectionObservers connectionObservers) {
        this.appContext = appContext;
        this.connection = connection;

        connectionObservers.addObserver(connectionObserver);
    }

    public void init() {
        SharedPreferences preferences = getPreferences();
        int savedVersionCode = preferences.getInt(PREF_KEY_APP_VERSION, 0);
        if (savedVersionCode != BuildConfig.VERSION_CODE) {
            clearPropHash();
            Log.i("ServerProps/init app upgraded hash cleared");
            if (!preferences.edit().putInt(PREF_KEY_APP_VERSION, BuildConfig.VERSION_CODE).commit()) {
                Log.e("ServerProps/init failed to update app version");
            }
        }
        loadProps();
    }

    @WorkerThread
    private synchronized SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = appContext.get().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    @WorkerThread
    public synchronized void onReceiveServerPropsHash(@NonNull String hash) {
        SharedPreferences preferences = getPreferences();
        String oldHash = preferences.getString(PREF_KEY_PROPS_HASH, null);
        if (oldHash != null && oldHash.equals(hash) && !propsExpired()) {
            return;
        }
        connection.requestServerProps();
    }

    @WorkerThread
    public synchronized void onReceiveServerProps(@NonNull Map<String, String> propMap, @NonNull String propsHash) {
        for (Prop prop : serverProps) {
            String value = propMap.get(prop.getKey());
            if (value != null) {
                prop.parse(value);
            }
        }
        saveProps(propsHash);
        setLastFetchTime();
    }

    private boolean propsExpired() {
        return System.currentTimeMillis() - getLastFetchTime() > PROPS_EXPIRATION_MS;
    }

    private long getLastFetchTime() {
        return getPreferences().getLong(PREF_KEY_LAST_PROPS_FETCH, 0);
    }

    private void setLastFetchTime() {
        getPreferences().edit().putLong(PREF_KEY_LAST_PROPS_FETCH, System.currentTimeMillis()).apply();
    }

    private void loadProps() {
        SharedPreferences preferences = getPreferences();
        for (Prop prop : serverProps) {
            prop.load(preferences);
        }
    }

    private void saveProps(@NonNull String propHash) {
        SharedPreferences preferences = getPreferences();
        for (Prop prop : serverProps) {
            prop.save(preferences);
        }
        if (!preferences.edit().putString(PREF_KEY_PROPS_HASH, propHash).commit()) {
            Log.e("ServerProps/saveProps: failed to save hash");
        }
    }

    private void clearPropHash() {
        if (!getPreferences().edit().remove(PREF_KEY_PROPS_HASH).commit()) {
            Log.e("ServerProps/saveProps: failed to clear hash");
        }
    }

    private BooleanProp createProp(@NonNull String key, boolean defaultValue) {
        BooleanProp prop = new BooleanProp(key, defaultValue);
        serverProps.add(prop);
        return prop;
    }

    private IntegerProp createProp(@NonNull String key, int defaultValue) {
        IntegerProp prop = new IntegerProp(key, defaultValue);
        serverProps.add(prop);
        return prop;
    }

    public synchronized boolean getIsInternalUser() {
        return propInternalUser.getValue();
    }
    
    public synchronized int getMaxGroupSize() {
        return propMaxGroupSize.getValue();
    }

    public synchronized boolean getGroupChatsEnabled() {
        return propGroupChatsEnabled.getValue();
    }

    public synchronized boolean getGroupFeedEnabled() {
        return propGroupFeedEnabled.getValue();
    }

    public synchronized int getSilentChatMessageCount() {
        return propSilentChatMessages.getValue();
    }
}
