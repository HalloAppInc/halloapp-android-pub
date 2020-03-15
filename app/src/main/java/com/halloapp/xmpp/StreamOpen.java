package com.halloapp.xmpp;

import com.halloapp.BuildConfig;

import org.jivesoftware.smack.packet.Nonza;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class StreamOpen implements Nonza {

    public static final String ELEMENT = "stream:stream";

    public static final String CLIENT_NAMESPACE = "jabber:client";
    public static final String SERVER_NAMESPACE = "jabber:server";

    /**
     * RFC 6120 § 4.7.5.
     */
    public static final String VERSION = "1.0";

    /**
     * RFC 6120 § 4.7.1.
     */
    private final String from;

    /**
     * RFC 6120 § 4.7.2.
     */
    private final String to;

    /**
     * RFC 6120 § 4.7.3.
     */
    private final String id;

    /**
     * RFC 6120 § 4.7.4.
     */
    private final String lang;

    /**
     * RFC 6120 § 4.8.2.
     */
    private final String contentNamespace;

    public StreamOpen(CharSequence to) {
        this(to, null, null, null, org.jivesoftware.smack.packet.StreamOpen.StreamContentNamespace.client);
    }

    public StreamOpen(CharSequence to, CharSequence from, String id) {
        this(to, from, id, "en", org.jivesoftware.smack.packet.StreamOpen.StreamContentNamespace.client);
    }

    public StreamOpen(CharSequence to, CharSequence from, String id, String lang, org.jivesoftware.smack.packet.StreamOpen.StreamContentNamespace ns) {
        this.to = StringUtils.maybeToString(to);
        this.from = StringUtils.maybeToString(from);
        this.id = id;
        this.lang = lang;
        switch (ns) {
            case client:
                this.contentNamespace = CLIENT_NAMESPACE;
                break;
            case server:
                this.contentNamespace = SERVER_NAMESPACE;
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String getNamespace() {
        return contentNamespace;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder();
        xml.halfOpenElement(getElementName());
        // We always want to state 'xmlns' for stream open tags.
        xml.attribute("xmlns", enclosingNamespace);

        xml.attribute("to", to);
        xml.attribute("xmlns:stream", "http://etherx.jabber.org/streams");
        xml.attribute("version", VERSION);
        xml.optAttribute("from", from);
        xml.optAttribute("id", id);
        xml.attribute("client_version", "Android" + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "D" : ""));
        xml.xmllangAttribute(lang);
        xml.rightAngleBracket();
        return xml;
    }

    public enum StreamContentNamespace {
        client,
        server
    }
}
