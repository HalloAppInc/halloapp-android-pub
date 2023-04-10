package com.halloapp.util.stats;

import androidx.annotation.NonNull;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.proto.log_events.Call;
import com.halloapp.proto.log_events.DecryptionReport;
import com.halloapp.proto.log_events.EventData;
import com.halloapp.proto.log_events.FabAction;
import com.halloapp.proto.log_events.GroupDecryptionReport;
import com.halloapp.proto.log_events.GroupHistoryReport;
import com.halloapp.proto.log_events.HomeDecryptionReport;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.proto.log_events.MediaDownload;
import com.halloapp.proto.log_events.MediaObjectDownload;
import com.halloapp.proto.log_events.MediaUpload;
import com.halloapp.proto.log_events.Permissions;
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

    public Observable<Void> sendEvent(@NonNull PushReceived pushReceived) {
        return sendEvent(EventData.newBuilder().setPushReceived(pushReceived));
    }

    public void sendEvent(@NonNull MediaUpload mediaUpload) {
        sendEvent(EventData.newBuilder().setMediaUpload(mediaUpload));
    }

    public void sendEvent(@NonNull MediaDownload mediaDownload) {
        sendEvent(EventData.newBuilder().setMediaDownload(mediaDownload));
    }

    public void sendEvent(@NonNull MediaObjectDownload mediaObjectDownload) {
        sendEvent(EventData.newBuilder().setMediaObjectDownload(mediaObjectDownload));
    }

    public void sendEvent(@NonNull MediaComposeLoad mediaComposeLoad) {
        sendEvent(EventData.newBuilder().setMediaComposeLoad(mediaComposeLoad));
    }

    public void sendEvent(@NonNull DecryptionReport decryptionReport) {
        sendEvent(EventData.newBuilder().setDecryptionReport(decryptionReport));
    }

    public void sendEvent(@NonNull Call call) {
        sendEvent(EventData.newBuilder().setCall(call));
    }

    public void sendEvent(@NonNull Permissions permissions) {
        sendEvent(EventData.newBuilder().setPermissions(permissions));
    }

    private Observable<Void> sendEvent(@NonNull EventData.Builder builder) {
        return sendEvents(Collections.singleton(builder));
    }

    public void sendFabActionEvent(FabAction.FabActionType type) {
        FabAction fabActionEvent = FabAction.newBuilder().setType(type).build();
        sendEvent(EventData.newBuilder().setFabAction(fabActionEvent));
    }

    public Observable<Void> sendDecryptionReports(@NonNull Collection<DecryptionReport> decryptionReports) {
        Collection<EventData.Builder> events = new ArrayList<>();
        for (DecryptionReport decryptionReport : decryptionReports) {
            events.add(EventData.newBuilder().setDecryptionReport(decryptionReport));
        }
        return sendEvents(events);
    }

    public Observable<Void> sendGroupDecryptionReports(@NonNull Collection<GroupDecryptionReport> groupDecryptionReports) {
        Collection<EventData.Builder> events = new ArrayList<>();
        for (GroupDecryptionReport groupDecryptionReport : groupDecryptionReports) {
            events.add(EventData.newBuilder().setGroupDecryptionReport(groupDecryptionReport));
        }
        return sendEvents(events);
    }

    public Observable<Void> sendGroupHistoryDecryptionReports(@NonNull Collection<GroupHistoryReport> groupHistoryReports) {
        Collection<EventData.Builder> events = new ArrayList<>();
        for (GroupHistoryReport groupHistoryReport : groupHistoryReports) {
            events.add(EventData.newBuilder().setGroupHistoryReport(groupHistoryReport));
        }
        return sendEvents(events);
    }

    public Observable<Void> sendHomeDecryptionReports(@NonNull Collection<HomeDecryptionReport> homeDecryptionReports) {
        Collection<EventData.Builder> events = new ArrayList<>();
        for (HomeDecryptionReport homeDecryptionReport : homeDecryptionReports) {
            events.add(EventData.newBuilder().setHomeDecryptionReport(homeDecryptionReport));
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
                builder.setVersion(Constants.FULL_VERSION);
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
