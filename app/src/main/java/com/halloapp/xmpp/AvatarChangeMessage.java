package com.halloapp.xmpp;

import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;

import java.util.List;
import java.util.Map;

public class AvatarChangeMessage implements ExtensionElement {

    public static final String ELEMENT = "avatar";
    public static final String NAMESPACE = "halloapp:user:avatar";

    private static final String ATTRIBUTE_USER_ID = "userid";
    private static final String ATTRIBUTE_AVATAR_ID = "id";

    public UserId userId;
    public String avatarId;

    public AvatarChangeMessage(UserId userId, String avatarId) {
        this.userId = userId;
        this.avatarId = avatarId;
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

    public static class Provider extends EmbeddedExtensionProvider<AvatarChangeMessage> {

        @Override
        protected AvatarChangeMessage createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final String rawUserId = Preconditions.checkNotNull(attributeMap.get(ATTRIBUTE_USER_ID));
            final String avatarId = attributeMap.get(ATTRIBUTE_AVATAR_ID);
            return new AvatarChangeMessage(new UserId(rawUserId), avatarId);
        }
    }
}
