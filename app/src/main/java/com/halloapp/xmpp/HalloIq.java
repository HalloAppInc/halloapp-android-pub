package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.calls.GetCallServersResponseIq;
import com.halloapp.xmpp.calls.StartCallResponseIq;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.halloapp.xmpp.privacy.PrivacyListsResponseIq;

public abstract class HalloIq {

    private String id;

    public HalloIq() {
    }

    public HalloIq(@NonNull String id) {
        this.id = id;
    }

    public abstract Iq.Builder toProtoIq();

    public static HalloIq fromProtoIq(Iq iq) {
        if (iq.hasWhisperKeys()) {
            return WhisperKeysResponseIq.fromProto(iq.getWhisperKeys());
        } else if (iq.hasGroupsStanza()) {
            return GroupsListResponseIq.fromProto(iq.getGroupsStanza());
        } else if (iq.hasGroupStanza()) {
            return GroupResponseIq.fromProto(iq.getGroupStanza());
        } else if (iq.hasPrivacyLists()) {
            return PrivacyListsResponseIq.fromProto(iq.getPrivacyLists());
        } else if (iq.hasInvitesResponse()) {
            return InvitesResponseIq.fromProto(iq.getInvitesResponse());
        } else if (iq.hasStartCallResult()) {
            return StartCallResponseIq.fromProto(iq.getStartCallResult());
        } else if (iq.hasGetCallServersResult()) {
            return GetCallServersResponseIq.fromProto(iq.getGetCallServersResult());
        } else if (iq.hasExternalSharePost()) {
            return ExternalShareResponseIq.fromProto(iq.getExternalSharePost());
        } else if (iq.hasExternalSharePostContainer()) {
            return ExternalShareRetrieveResponseIq.fromProto(iq.getExternalSharePostContainer());
        }
        Log.w("Using empty result IQ due to unrecognized result IQ " + ProtoPrinter.toString(iq));
        return new EmptyResultIq();
    }

    private static class EmptyResultIq extends HalloIq {

        @Override
        public Iq.Builder toProtoIq() {
            return null;
        }
    }
}
