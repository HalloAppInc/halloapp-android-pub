package com.halloapp.xmpp;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InvitesRequestIq extends IQ {

    public static final String ELEMENT = "invites";
    public static final String NAMESPACE = "halloapp:invites";

    private List<String> invitedNumbers;

    public static InvitesRequestIq createSendInviteIq(@NonNull Collection<String> invitedNumbers) {
        InvitesRequestIq requestIq = new InvitesRequestIq();
        requestIq.setType(Type.set);
        requestIq.invitedNumbers = new ArrayList<>(invitedNumbers);
        return requestIq;
    }

    public static InvitesRequestIq createGetInviteIq() {
        InvitesRequestIq requestIq = new InvitesRequestIq();
        requestIq.setType(Type.get);
        return requestIq;
    }

    private InvitesRequestIq() {
        super(ELEMENT, NAMESPACE);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        if (invitedNumbers == null || invitedNumbers.isEmpty()) {
            xml.setEmptyElement();
            return xml;
        }
        xml.rightAngleBracket();
        for (String invitedNumber : invitedNumbers) {
            if (TextUtils.isEmpty(invitedNumber)) {
                continue;
            }
            xml.halfOpenElement("invite");
            xml.attribute("phone", invitedNumber);
            xml.closeEmptyElement();
        }
        return xml;
    }
}
