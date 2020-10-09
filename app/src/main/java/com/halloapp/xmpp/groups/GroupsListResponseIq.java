package com.halloapp.xmpp.groups;

import com.google.protobuf.ByteString;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.proto.server.GroupStanza;
import com.halloapp.proto.server.GroupsStanza;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.WhisperKeys;
import com.halloapp.util.Log;
import com.halloapp.util.Xml;
import com.halloapp.xmpp.HalloIq;
import com.halloapp.xmpp.WhisperKeysResponseIq;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupsListResponseIq extends HalloIq {

    public static final String ELEMENT = "groups";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_GID = "gid";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_AVATAR = "avatar";

    public final List<GroupInfo> groupInfos = new ArrayList<>();

    protected GroupsListResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String elementName = parser.getName();
            if (GroupResponseIq.ELEMENT.equals(elementName)) {
                GroupId groupId = new GroupId(parser.getAttributeValue("", ATTRIBUTE_GID));
                String name = parser.getAttributeValue("", ATTRIBUTE_NAME);
                String description = parser.getAttributeValue("", ATTRIBUTE_DESCRIPTION);
                String avatar = parser.getAttributeValue("", ATTRIBUTE_AVATAR);
                GroupInfo groupInfo = new GroupInfo(groupId, name, description, avatar, null);
                groupInfos.add(groupInfo);
            }
            Xml.skip(parser);
        }
    }

    private GroupsListResponseIq(List<GroupInfo> groupInfos) {
        super(ELEMENT, NAMESPACE);
        this.groupInfos.addAll(groupInfos);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    public static GroupsListResponseIq fromProto(GroupsStanza groupsStanza) {
        List<GroupInfo> groupInfos = new ArrayList<>();
        for (GroupStanza groupStanza : groupsStanza.getGroupStanzasList()) {
            groupInfos.add(new GroupInfo(new GroupId(groupStanza.getGid()), groupStanza.getName(), null, groupStanza.getAvatarId(), new ArrayList<>()));
        }
        return new GroupsListResponseIq(groupInfos);
    }

    public static class Provider extends IQProvider<GroupsListResponseIq> {

        @Override
        public GroupsListResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new GroupsListResponseIq(parser);
        }
    }

}
