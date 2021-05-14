package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.Preferences;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.halloapp.xmpp.privacy.PrivacyListsResponseIq;

public abstract class HalloIq extends HalloStanza {

    private static final Preferences preferences = Preferences.getInstance();

    public HalloIq() {
        super(preferences.getAndIncrementShortId());
    }

    public HalloIq(@NonNull String id) {
        super(id);
    }

    public abstract Iq toProtoIq();

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
        }
        Log.w("Using empty result IQ due to unrecognized result IQ " + ProtoPrinter.toString(iq));
        return new EmptyResultIq(iq.getId());
    }

    private static class EmptyResultIq extends HalloIq {
        protected EmptyResultIq(String id) {
            setStanzaId(id);
        }

        @Override
        public Iq toProtoIq() {
            return null;
        }
    }
}
