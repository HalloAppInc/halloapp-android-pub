package com.halloapp.xmpp.privacy;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.UidElement;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivacyList implements ExtensionElement {

    static final String NAMESPACE = "halloapp:user:privacy";
    static final String ELEMENT = "privacy_list";

    static final String ELEMENT_UID = "uid";

    static final String ATTRIBUTE_TYPE = "type";

    static final String TYPE_ADD = "add";
    static final String TYPE_DELETE = "delete";

    public final Map<UserId, String> typeMap = new HashMap<>();
    public final List<UserId> userIds = new ArrayList<>();

    public @Type String type;

    @StringDef({Type.ALL, Type.EXCEPT, Type.ONLY, Type.MUTE, Type.BLOCK, Type.INVALID})
    public @interface Type {
        String INVALID = "invalid";
        String ALL = "all"; // All mutually connected people
        String EXCEPT = "except"; // Blacklist
        String ONLY = "only"; // Whitelist
        String MUTE = "mute"; // Hide content from specific people (feed only)
        String BLOCK = "block"; // Blocks all communication
    }

    PrivacyList(XmlPullParser parser) throws IOException, XmlPullParserException {
        @Type String type = Type.INVALID;
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeName(i).equalsIgnoreCase(ATTRIBUTE_TYPE)) {
                type = parser.getAttributeValue(i);
                break;
            }
        }
        this.type = type;
        if (Type.INVALID.equals(type)) {
            return;
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_UID.equals(name)) {
                String addType = parser.getAttributeValue(null, ATTRIBUTE_TYPE);
                UserId userId = new UserId(Xml.readText(parser));
                userIds.add(userId);
                if (TYPE_ADD.equalsIgnoreCase(addType)) {
                    typeMap.put(userId, TYPE_ADD);
                } else if (TYPE_DELETE.equalsIgnoreCase(addType)) {
                    typeMap.put(userId, TYPE_DELETE);
                }
            } else {
                Xml.skip(parser);
            }
        }
    }

    PrivacyList(com.halloapp.proto.server.PrivacyList privacyList) {
        this.type = privacyList.getType().name().toLowerCase();
        for (UidElement uidElement : privacyList.getUidElementsList()) {
            UserId userId = new UserId(Long.toString(uidElement.getUid()));
            userIds.add(userId);
            if (uidElement.getAction().equals(UidElement.Action.ADD)) {
                typeMap.put(userId, TYPE_ADD);
            } else if (uidElement.getAction().equals(UidElement.Action.DELETE)) {
                typeMap.put(userId, TYPE_DELETE);
            }
        }
    }

    @NonNull
    public List<UserId> getUserIds() {
        return userIds;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    private void appendUID(XmlStringBuilder xmlStringBuilder, String type, UserId uid) {
        xmlStringBuilder.halfOpenElement(ELEMENT_UID);
        xmlStringBuilder.attribute(ATTRIBUTE_TYPE, type);
        xmlStringBuilder.rightAngleBracket();
        xmlStringBuilder.append(uid.rawId());
        xmlStringBuilder.closeElement(ELEMENT_UID);
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        final XmlStringBuilder buf = new XmlStringBuilder(enclosingNamespace);
        buf.halfOpenElement(ELEMENT);
        buf.xmlnsAttribute(NAMESPACE);
        buf.attribute(ATTRIBUTE_TYPE, type);
        buf.rightAngleBracket();
        for (UserId userId : userIds) {
            String type = typeMap.get(userId);
            if (type != null) {
                appendUID(buf, type, userId);
            }
        }
        buf.closeElement(ELEMENT);
        return buf;
    }
}
