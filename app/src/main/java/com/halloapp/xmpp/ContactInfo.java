package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.Contact;
import com.halloapp.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Locale;

public class ContactInfo {

    private static final String ELEMENT_USER_ID = "userid";
    private static final String ELEMENT_ROLE = "role";
    private static final String ELEMENT_RAW = "raw";
    private static final String ELEMENT_NORMALIZED = "normalized";
    private static final String ELEMENT_AVATAR_ID = "avatarid";
    private static final String ELEMENT_CONTACT_HALLO_NAME = "name";

    public String userId;
    public String role;
    public String phone;
    public String normalizedPhone;
    public String avatarId;
    public String halloName;

    ContactInfo(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_RAW.equals(name)) {
                phone = Xml.readText(parser);
            } else if (ELEMENT_NORMALIZED.equals(name)) {
                normalizedPhone = Xml.readText(parser);
            } else if (ELEMENT_ROLE.equals(name)) {
                role = Xml.readText(parser);
            } else if (ELEMENT_USER_ID.equals(name)) {
                userId = Xml.readText(parser);
            } else if (ELEMENT_AVATAR_ID.equals(name)) {
                avatarId = Xml.readText(parser);
            } else if (ELEMENT_CONTACT_HALLO_NAME.equals(name)) {
                halloName = Xml.readText(parser);
            } else {
                Xml.skip(parser);
            }
        }
    }

    ContactInfo(@NonNull Contact contact) {
        if (contact.getRaw() != null) {
            phone = contact.getRaw();
        }
        if (contact.getNormalized() != null) {
            normalizedPhone = contact.getNormalized();
        }
        if (contact.getRole() != null) {
            role = contact.getRole().name().toLowerCase(Locale.US);
        }
        if (contact.getUid() != 0) {
            userId = Long.toString(contact.getUid());
        }
        if (contact.getAvatarId() != null) {
            avatarId = contact.getAvatarId();
        }
        if (contact.getName() != null) {
            halloName = contact.getName();
        }
    }
}
