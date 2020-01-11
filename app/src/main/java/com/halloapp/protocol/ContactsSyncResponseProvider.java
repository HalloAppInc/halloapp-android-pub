package com.halloapp.protocol;

import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ContactsSyncResponseProvider extends IQProvider<ContactsSyncResponse> {

    @Override
    public ContactsSyncResponse parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
        return new ContactsSyncResponse(parser);
    }
}
