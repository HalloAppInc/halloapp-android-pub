package com.halloapp.xmpp.groups;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupChangeMessage implements ExtensionElement {

    public static final String ELEMENT = "group";
    public static final String NAMESPACE = "halloapp:groups";

    private static final String ATTRIBUTE_GROUP_ID = "gid";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_AVATAR_ID = "avatar";
    private static final String ATTRIBUTE_SENDER_ID = "sender";
    private static final String ATTRIBUTE_SENDER_NAME = "sender_name";

    @StringDef({Action.MODIFY_MEMBERS, Action.CREATE, Action.LEAVE, Action.MODIFY_ADMINS, Action.CHANGE_NAME, Action.CHANGE_AVATAR, Action.AUTO_PROMOTE, Action.INVALID})
    public @interface Action {
        String INVALID = "invalid";
        String CREATE = "create";
        String MODIFY_MEMBERS = "modify_members";
        String LEAVE = "leave";
        String MODIFY_ADMINS = "modify_admins";
        String CHANGE_NAME = "change_name";
        String CHANGE_AVATAR = "change_avatar";
        String AUTO_PROMOTE = "auto_promote_admins";
    }

    public GroupId groupId;
    public @Action String action;
    public String name;
    public String avatarId;
    public UserId sender;
    public String senderName;
    public List<MemberElement> members;

    public GroupChangeMessage(
            @NonNull GroupId groupId,
            @NonNull String action,
            String name,
            String avatarId,
            @Nullable UserId sender,
            @Nullable String senderName,
            @NonNull List<MemberElement> members
    ) {
        this.groupId = groupId;
        this.action = action;
        this.name = name;
        this.avatarId = avatarId;
        this.sender = sender;
        this.senderName = senderName;
        this.members = members;
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

    public static class Provider extends EmbeddedExtensionProvider<GroupChangeMessage> {

        @Override
        protected GroupChangeMessage createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            final GroupId groupId = new GroupId(Preconditions.checkNotNull(attributeMap.get(ATTRIBUTE_GROUP_ID)));
            final @Action String action = Preconditions.checkNotNull(attributeMap.get(ATTRIBUTE_ACTION));
            final String name = attributeMap.get(ATTRIBUTE_NAME);
            final String avatarId = attributeMap.get(ATTRIBUTE_AVATAR_ID);
            final String senderId = attributeMap.get(ATTRIBUTE_SENDER_ID);
            final String senderName = attributeMap.get(ATTRIBUTE_SENDER_NAME);
            final UserId senderUserId = senderId == null ? null : new UserId(senderId);

            List<MemberElement> members = new ArrayList<>();
            for (ExtensionElement elem : content) {
                if (elem instanceof MemberElement) {
                    members.add((MemberElement)elem);
                }
            }

            return new GroupChangeMessage(groupId, action, name, avatarId, senderUserId, senderName, members);
        }
    }
}
