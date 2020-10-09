package com.halloapp.xmpp;

import com.halloapp.proto.server.Iq;
import com.halloapp.util.Log;
import com.halloapp.xmpp.groups.GroupResponseIq;
import com.halloapp.xmpp.groups.GroupsListResponseIq;

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
        }
        Log.w("Unrecognized result IQ " + iq);
        return null;
    }
}
