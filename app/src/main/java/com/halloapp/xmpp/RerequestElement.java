package com.halloapp.xmpp;

import com.google.protobuf.ByteString;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.Msg;
import com.halloapp.proto.server.Rerequest;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

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
    public final UserId originalSender;

    public RerequestElement(String identityKey, UserId originalSender) {
        this.id = RandomId.create();
        this.identityKey = identityKey;
        this.originalSender = originalSender;
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
        byte[] identityKey = null;
        try {
            identityKey = EncryptedKeyStore.getInstance().getMyPublicEd25519IdentityKey().getKeyMaterial();
        } catch (Exception e) {
            Log.w("Failed to get identity key bytes for rerequest", e);
        }
        
        Rerequest.Builder builder =  Rerequest.newBuilder();
        builder.setId(id);
        if (identityKey != null) {
            builder.setIdentityKey(ByteString.copyFrom(identityKey));
        }

        return Msg.newBuilder()
                .setId(id)
                .setToUid(Long.parseLong(originalSender.rawId()))
                .setRerequest(builder)
                .build();
    }

    public static class Provider extends EmbeddedExtensionProvider<RerequestElement> {

        @Override
        protected RerequestElement createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String id = attributeMap.get(ATTRIBUTE_ID);
            final String identityKey = attributeMap.get(ATTRIBUTE_IDENTITY_KEY);
            return new RerequestElement(identityKey, null);
        }
    }
}
