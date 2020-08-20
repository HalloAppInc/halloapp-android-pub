package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GroupAvatarIq extends IQ {

    public static final String ELEMENT = "group_avatar";

    private static final String ATTRIBUTE_GROUP_ID = "gid";

    public static final String NAMESPACE = "halloapp:groups";

    final GroupId groupId;
    final String base64;

    GroupAvatarIq(Jid to, @NonNull GroupId groupId, @NonNull String base64) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        setTo(to);
        this.groupId = groupId;
        this.base64 = base64;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute(ATTRIBUTE_GROUP_ID, groupId.rawId());
        xml.rightAngleBracket();
        xml.append(base64);

        return xml;
    }
}
