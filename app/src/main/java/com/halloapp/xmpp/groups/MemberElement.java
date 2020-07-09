package com.halloapp.xmpp.groups;

import androidx.annotation.StringDef;

import com.halloapp.contacts.UserId;
import com.halloapp.util.Preconditions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemberElement implements ExtensionElement {

    public static final String NAMESPACE = "halloapp:groups";
    public static final String ELEMENT = "member";

    private static final String ELEMENT_UID = "uid";

    private static final String ATTRIBUTE_UID = "uid"; // only mandatory one
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_RESULT = "result";
    private static final String ATTRIBUTE_REASON = "reason";

    public final Map<UserId, String> typeMap = new HashMap<>();

    public UserId uid;
    public @Type String type;
    public String name;
    public @Action String action;
    public String result;
    public String reason;

    @StringDef({Type.ADMIN, Type.MEMBER, Type.INVALID})
    public @interface Type {
        String INVALID = "invalid";
        String ADMIN = "admin";
        String MEMBER = "member";
    }

    @StringDef({Action.ADD, Action.REMOVE, Action.LEAVE, Action.PROMOTE, Action.DEMOTE, Action.INVALID})
    public @interface Action {
        String INVALID = "invalid";
        String ADD = "add";
        String REMOVE = "remove";
        String LEAVE = "leave";
        String PROMOTE = "promote";
        String DEMOTE = "demote";
    }

    MemberElement(UserId uid) {
        this.uid = uid;
    }

    MemberElement(UserId uid, String action) {
        this.uid = uid;
        this.action = action;
    }

    MemberElement(XmlPullParser parser) throws IOException, XmlPullParserException {
        String parsedUid = parser.getAttributeValue("", ATTRIBUTE_UID);
        uid = parsedUid == null ? null : new UserId(parsedUid);
        type = parser.getAttributeValue("", ATTRIBUTE_TYPE);
        name = parser.getAttributeValue("", ATTRIBUTE_NAME);
        action = parser.getAttributeValue("", ATTRIBUTE_ACTION);
        result = parser.getAttributeValue("", ATTRIBUTE_RESULT);
        reason = parser.getAttributeValue("", ATTRIBUTE_REASON);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        final XmlStringBuilder buf = new XmlStringBuilder(enclosingNamespace);
        buf.halfOpenElement(ELEMENT);
        buf.xmlnsAttribute(NAMESPACE);
        buf.attribute(ATTRIBUTE_UID, Preconditions.checkNotNull(uid).rawId());
        buf.closeEmptyElement();
        return buf;
    }

}
