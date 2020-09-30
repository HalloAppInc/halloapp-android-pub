package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

public class NameMessage implements ExtensionElement {

    public static final String ELEMENT = "name";
    public static final String NAMESPACE = "halloapp:users:name";

    private static final String ATTRIBUTE_USER_ID = "uid";

    public UserId userId;
    public String name;

    public NameMessage(@NonNull UserId userId, @NonNull String name) {
        this.userId = userId;
        this.name = name;
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
        return null;
    }

    public static class Provider extends ExtensionElementProvider<NameMessage> {

        @Override
        public final NameMessage parse(XmlPullParser parser, int initialDepth) throws Exception {
            final String rawUserId = parser.getAttributeValue(null, ATTRIBUTE_USER_ID);
            final UserId userId = new UserId(Preconditions.checkNotNull(rawUserId));
            String name = Xml.readText(parser);
            return new NameMessage(userId, name);
        }
    }
}
