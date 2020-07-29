package com.halloapp.xmpp.groups;

import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.util.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupsListResponseIq extends IQ {

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

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Provider extends IQProvider<GroupsListResponseIq> {

        @Override
        public GroupsListResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new GroupsListResponseIq(parser);
        }
    }

}
