package com.halloapp.util.stats;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.proto.log_events.DecryptionReport;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.proto.log_events.MediaDownload;
import com.halloapp.proto.log_events.MediaUpload;
import com.halloapp.proto.log_events.Platform;
import com.halloapp.proto.log_events.PushReceived;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;

import java.util.ArrayList;
import java.util.Collection;
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

    public void sendEvent(@NonNull DecryptionReport decryptionReport) {
        sendEvent(EventData.newBuilder().setDecryptionReport(decryptionReport));
    }

    private void sendEvent(@NonNull EventData.Builder builder) {
        sendEvents(Collections.singleton(builder));
    }

    public Observable<Void> sendDecryptionReports(@NonNull Collection<DecryptionReport> decryptionReports) {
        Collection<EventData.Builder> events = new ArrayList<>();
        for (DecryptionReport decryptionReport : decryptionReports) {
            events.add(EventData.newBuilder().setDecryptionReport(decryptionReport));
        }
        return sendEvents(events);
    }

    private Observable<Void> sendEvents(@NonNull Collection<EventData.Builder> builders) {
        MutableObservable<Void> ret = new MutableObservable<>();
        bgWorkers.execute(() -> {
            if (!me.isRegistered()) {
                return;
            }
            Collection<EventData> events = new ArrayList<>();
            for (EventData.Builder builder : builders) {
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
                events.add(builder.build());
            }
            connection.sendEvents(events).onResponse(ret::setResponse).onError(ret::setException);
        });
        return ret;
    }

}
