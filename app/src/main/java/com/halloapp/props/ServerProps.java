package com.halloapp.props;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.ConnectionObservers;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerProps {

    private static final String PREFS_NAME = "props";

    private static final String PREF_KEY_PROPS_HASH = "props:version_hash";

    private static final String PROP_INTERNAL_USER = "dev";
    private static final String PROP_GROUPS_ENABLED = "groups";
    private static final String PROP_MAX_GROUP_SIZE = "max_group_size";

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
    private final BooleanProp propGroupsEnabled = createProp(PROP_GROUPS_ENABLED, false);

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
        if (oldHash != null && oldHash.equals(hash)) {
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

    public synchronized boolean getGroupsEnabled() {
        return propGroupsEnabled.getValue();
    }
}
