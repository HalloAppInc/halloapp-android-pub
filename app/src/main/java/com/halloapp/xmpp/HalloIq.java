package com.halloapp.xmpp;

import com.halloapp.BuildConfig;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PrivacyLists;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;
import com.halloapp.xmpp.invites.InvitesResponseIq;
import com.halloapp.xmpp.privacy.PrivacyListsResponseIq;

import org.jivesoftware.smack.packet.IQ;

/**
 * Temporarily extends Smack's IQ to aid in transition from xmpp to protobuf
 */

public abstract class HalloIq extends IQ {
    public HalloIq(HalloIq iq) {
        super(iq);
    }

    protected HalloIq(String childElementName) {
        super(childElementName);
    }

    protected HalloIq(String childElementName, String childElementNamespace) {
        super(childElementName, childElementNamespace);
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
        Log.w("Using empty result IQ due to unrecognized result IQ " + iq);
        return new EmptyResultIq(iq.getId());
    }

    private static class EmptyResultIq extends HalloIq {
        protected EmptyResultIq(String id) {
            super("");
            setStanzaId(id);
        }

        @Override
        public Iq toProtoIq() {
            return null;
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            return null;
        }
    }
}
