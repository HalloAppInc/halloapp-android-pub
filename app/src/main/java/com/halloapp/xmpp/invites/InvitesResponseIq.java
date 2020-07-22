package com.halloapp.xmpp.invites;

import androidx.annotation.Nullable;

import com.halloapp.util.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InvitesResponseIq extends IQ {

    public static final String ELEMENT = "invites";
    public static final String NAMESPACE = "halloapp:invites";

    private static final String ELEMENT_INVITE = "invite";

    @Nullable Integer invitesLeft;

    final Map<String, Integer> failedInvites = new HashMap<>();
    final Set<String> successfulInvites = new HashSet<>();

    public @interface Result {
        int UNKNOWN = -1;
        int SUCCESS = 0;
        int INVALID_NUMBER = 1;
        int NO_INVITES_LEFT = 2;
        int EXISTING_USER = 3;
        int NO_ACCOUNT = 4;
    }

    private @interface ReasonResponse {
        String INVALID_NUMBER = "invalid_number";
        String NO_INVITES_LEFT = "no_invites_left";
        String EXISTING_USER = "existing_user";
        String NO_ACCOUNT = "no_account";
    }

    protected InvitesResponseIq(XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        try {
            String invitesLeftStr = parser.getAttributeValue(null, "invites_left");
            if (invitesLeftStr != null) {
                invitesLeft = Integer.parseInt(invitesLeftStr);
            }
        } catch (NumberFormatException e) {
            Log.e("InvitesResponseIq parsing, invalid number for invites left", e);
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_INVITE.equals(name)) {
                String phoneNumber = parser.getAttributeValue(null, "phone");
                String resultStr = parser.getAttributeValue(null, "result");
                String reasonStr = parser.getAttributeValue(null, "reason");
                @Result int result = parseResult(resultStr, reasonStr);
                if (phoneNumber != null) {
                    if (result == Result.SUCCESS) {
                        successfulInvites.add(phoneNumber);
                    } else {
                        failedInvites.put(phoneNumber, result);
                    }
                }
            }
            Xml.skip(parser);
        }
    }

    private @Result int parseResult(@Nullable String resultStr, @Nullable String reasonStr) {
        if ("ok".equals(resultStr)) {
            return Result.SUCCESS;
        }
        if (resultStr == null || reasonStr == null) {
            return Result.UNKNOWN;
        }
        switch (reasonStr) {
            case ReasonResponse.INVALID_NUMBER:
                return Result.INVALID_NUMBER;
            case ReasonResponse.NO_INVITES_LEFT:
                return Result.NO_INVITES_LEFT;
            case ReasonResponse.EXISTING_USER:
                return Result.EXISTING_USER;
            case ReasonResponse.NO_ACCOUNT:
                return Result.NO_ACCOUNT;
            default:
                return Result.UNKNOWN;
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

    public static class Provider extends IQProvider<InvitesResponseIq> {

        @Override
        public InvitesResponseIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new InvitesResponseIq(parser);
        }
    }
}
