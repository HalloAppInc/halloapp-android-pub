package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.proto.server.ClientVersion;
import com.halloapp.proto.server.Iq;
import com.halloapp.util.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jxmpp.jid.Jid;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class SecondsToExpirationIq extends HalloIq {

    final static String ELEMENT = "client_version";
    final static String NAMESPACE = "halloapp:client:version";

    private final static String ELEMENT_VERSION = "version";
    private final static String ELEMENT_SECONDS_LEFT = "seconds_left";

    Integer secondsLeft = null;

    SecondsToExpirationIq(int secondsLeft) {
        super(ELEMENT, NAMESPACE);
        this.secondsLeft = secondsLeft;
    }

    SecondsToExpirationIq(@NonNull Jid to) {
        super(ELEMENT, NAMESPACE);
        setType(Type.get);
        setTo(to);
    }

    private SecondsToExpirationIq(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
        super(ELEMENT, NAMESPACE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = parser.getName();
            if (ELEMENT_SECONDS_LEFT.equals(name)) {
                try {
                    secondsLeft = Integer.parseInt(Xml.readText(parser));
                } catch (NumberFormatException e) {
                    Log.e("seconds to expiration", e);
                }
            } else {
                Xml.skip(parser);
            }
        }
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.openElement(ELEMENT_VERSION);
        xml.append(Constants.USER_AGENT);
        xml.closeElement(ELEMENT_VERSION);
        return xml;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setClientVersion(
                        ClientVersion.newBuilder()
                                .setVersion(BuildConfig.VERSION_NAME)
                                .build())
                .build();
    }

    public static SecondsToExpirationIq fromProto(ClientVersion clientVersion) {
        return new SecondsToExpirationIq((int)clientVersion.getExpiresInSeconds());
    }

    public static class Provider extends IQProvider<SecondsToExpirationIq> {

        @Override
        public SecondsToExpirationIq parse(XmlPullParser parser, int initialDepth) throws IOException, XmlPullParserException {
            return new SecondsToExpirationIq(parser);
        }
    }
}
