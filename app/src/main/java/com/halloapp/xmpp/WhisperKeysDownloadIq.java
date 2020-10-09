package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class WhisperKeysDownloadIq extends HalloIq {

    private static final String ELEMENT = "whisper_keys";
    private static final String NAMESPACE = "halloapp:whisper:keys";

    private static final String ATTRIBUTE_USERNAME = "username"; // TODO(jack): Remove when backend switches over to uid
    private static final String ATTRIBUTE_UID = "uid";

    private final String forUser;
    private final UserId userId;

    WhisperKeysDownloadIq(@NonNull Jid to, @NonNull String forUser, @NonNull UserId userId) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        setTo(to);
        this.forUser = forUser;
        this.userId = userId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute(ATTRIBUTE_USERNAME, forUser);
        xml.attribute(ATTRIBUTE_UID, userId.rawId());
        xml.attribute("type", "get");

        xml.rightAngleBracket();
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder().setId(getStanzaId()).setWhisperKeys(WhisperKeys.newBuilder().setAction(WhisperKeys.Action.GET).setUid(Long.parseLong(userId.rawId())).build()).build();
    }
}

