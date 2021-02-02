package com.halloapp.util.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.log_events.EventDataOrBuilder;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.proto.log_events.MediaDownload;
import com.halloapp.proto.log_events.MediaUpload;
import com.halloapp.proto.log_events.MediaUploadOrBuilder;
import com.halloapp.proto.log_events.Platform;
import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.proto.server.ClientLog;
import com.halloapp.proto.server.Event;
import com.halloapp.proto.server.EventOrBuilder;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.Collections;

public class Events {

    private static Events instance;

    public static Events getInstance() {
        if (instance == null) {
            synchronized (Events.class) {
                if (instance == null) {
                    instance = new Events(
                            Me.getInstance(),
                            BgWorkers.getInstance(),
                            Connection.getInstance());
                }
            }
        }
        return instance;
    }

    private final Me me;
    private final BgWorkers bgWorkers;
    private final Connection connection;

    Events(@NonNull Me me, @NonNull BgWorkers bgWorkers, @NonNull Connection connection) {
        this.me = me;
        this.bgWorkers = bgWorkers;
        this.connection = connection;
    }

    public void sendEvent(@NonNull PushReceived pushReceived) {
        sendEvent(EventData.newBuilder().setPushReceived(pushReceived));
    }

    public void sendEvent(@NonNull MediaUpload mediaUpload) {
        sendEvent(EventData.newBuilder().setMediaUpload(mediaUpload));
    }

    public void sendEvent(@NonNull MediaDownload mediaDownload) {
        sendEvent(EventData.newBuilder().setMediaDownload(mediaDownload));
    }

    public void sendEvent(@NonNull MediaComposeLoad mediaComposeLoad) {
        sendEvent(EventData.newBuilder().setMediaComposeLoad(mediaComposeLoad));
    }

    private void sendEvent(@NonNull EventData.Builder builder) {
        bgWorkers.execute(() -> {
            if (!me.isRegistered()) {
                return;
            }
            builder.setPlatform(Platform.ANDROID);
            builder.setVersion(BuildConfig.VERSION_NAME);
            String uidStr = me.getUser();
            if (uidStr != null) {
                try {
                    builder.setUid(Long.parseLong(uidStr));
                } catch (NumberFormatException e) {
                    Log.e("Events/createBaseEvent uid not a long");
                    return;
                }
            }
            connection.sendEvents(Collections.singleton(builder.build()));
        });
    }

}
