package com.halloapp.xmpp;

import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Rerequest;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;
import java.util.Map;

public class RerequestElement implements ExtensionElement {
    static final String NAMESPACE = "halloapp:enc:rereq";
    static final String ELEMENT = "rerequest";

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_IDENTITY_KEY = "identity_key";

    public final String id;
    public final String identityKey;

    public RerequestElement(String id, String identityKey) {
        this.id = id;
        this.identityKey = identityKey;
    }

    public static RerequestElement from(Message message) {
        return message.getExtension(ELEMENT, NAMESPACE);
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.optAttribute(ATTRIBUTE_ID, id);
        xml.optAttribute(ATTRIBUTE_IDENTITY_KEY, identityKey);
        xml.closeEmptyElement();
        return xml;
    }

    public Msg toProto() {
        Rerequest rerequest =  Rerequest.newBuilder().setId(id).build();
        return Msg.newBuilder()
                .setId(id)
                .setRerequest(rerequest)
                .build();
    }

    public static class Provider extends EmbeddedExtensionProvider<RerequestElement> {

        @Override
        protected RerequestElement createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String id = attributeMap.get(ATTRIBUTE_ID);
            final String identityKey = attributeMap.get(ATTRIBUTE_IDENTITY_KEY);
            return new RerequestElement(id, identityKey);
        }
    }
}
