package com.halloapp.xmpp.privacy;

import androidx.annotation.Nullable;

import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.privacy.PrivacyList;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SetPrivacyListIq extends IQ {

    public static final String ELEMENT = "privacy_list";
    public static final String NAMESPACE = "halloapp:user:privacy";

    private @PrivacyList.Type String type;

    private List<UserId> usersAdd = new ArrayList<>();
    private List<UserId> usersDelete = new ArrayList<>();

    protected SetPrivacyListIq(@PrivacyList.Type String listType, @Nullable Collection<UserId> usersToAdd, @Nullable Collection<UserId> usersToDelete) {
        super(ELEMENT, NAMESPACE);
        setType(Type.set);
        this.type = listType;
        if (usersToAdd != null) {
            this.usersAdd.addAll(usersToAdd);
        }
        if (usersToDelete != null) {
            this.usersDelete.addAll(usersToDelete);
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute("type", type);
        xml.rightAngleBracket();
        for (UserId userId : usersAdd) {
            appendUID(xml, "add", userId);
        }
        for (UserId userId : usersDelete) {
            appendUID(xml, "delete", userId);
        }
        return xml;
    }

    private void appendUID(XmlStringBuilder xmlStringBuilder, String type, UserId uid) {
        xmlStringBuilder.halfOpenElement("uid");
        xmlStringBuilder.attribute("type", type);
        xmlStringBuilder.rightAngleBracket();
        xmlStringBuilder.append(uid.rawId());
        xmlStringBuilder.closeElement("uid");
    }
}
