package com.halloapp.xmpp.groups;

import com.halloapp.id.GroupId;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.Log;
import com.halloapp.util.Xml;
import com.halloapp.xmpp.HalloIq;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupResponseIq extends HalloIq {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_GID = "gid";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_AVATAR = "avatar";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_RESULT = "result";

    public final GroupId groupId;
    public final String name;
    public final String description;
    public final String avatar;
    public final String action;
    public final String result;

    public final List<MemberElement> memberElements;

    protected GroupResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);
        groupId = new GroupId(parser.getAttributeValue("", ATTRIBUTE_GID));
        name = parser.getAttributeValue("", ATTRIBUTE_NAME);
        description = parser.getAttributeValue("", ATTRIBUTE_DESCRIPTION);
        avatar = parser.getAttributeValue("", ATTRIBUTE_AVATAR);
        action = parser.getAttributeValue("", ATTRIBUTE_ACTION);
        result = parser.getAttributeValue("", ATTRIBUTE_RESULT);
        memberElements = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (MemberElement.ELEMENT.equals(name)) {
                MemberElement memberElement = new MemberElement(parser);
                memberElements.add(memberElement);
            }
            Xml.skip(parser);
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    @Override
    public Iq toProtoIq() {
        return null;
    }

    // TODO(jack): What if IQ interface had getProvider() function to make adding providers easier?
    public static class Provider extends IQProvider<GroupResponseIq> {

        @Override
        public GroupResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new GroupResponseIq(parser);
        }
    }

}
