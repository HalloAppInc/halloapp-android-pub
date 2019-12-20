package com.halloapp.protocol;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;

import com.halloapp.util.Xml;

import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class PublishedEntry {

    public static final String ELEMENT_ENTRY = "entry";
    public static final String ELEMENT_USER = "username";
    public static final String ELEMENT_URL = "imageUrl";
    public static final String ELEMENT_TEXT = "text";

    private static final String NAMESPACE = "http://halloapp.com/published-entry";

    public final String id;
    public final String user;
    public final String text;
    public final String url;

    public PublishedEntry(String id, String user, String text, String url) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.url = url;
    }

    public static @NonNull List<PublishedEntry> getPublishedItems(@NonNull ItemPublishEvent<PayloadItem<SimplePayload>> items) {
        final List<PublishedEntry> entries = new ArrayList<>();
        for (PayloadItem<SimplePayload> item : items.getItems()) {
            final String xml = item.getPayload().toXML(null);
            final XmlPullParser parser = android.util.Xml.newPullParser();
            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));
                parser.nextTag();
                final PublishedEntry entry = readEntry(parser, item.getId());
                if (entry.valid()) {
                    entries.add(entry);
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
        return entries;
    }

    public @NonNull String toXml() {
        final XmlSerializer serializer = android.util.Xml.newSerializer();
        final StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.setPrefix("", NAMESPACE);
            serializer.startTag(NAMESPACE, ELEMENT_ENTRY);
            serializer.startTag(NAMESPACE, ELEMENT_USER);
            serializer.text(user);
            serializer.endTag(NAMESPACE, ELEMENT_USER);
            if (text != null) {
                serializer.startTag(NAMESPACE, ELEMENT_TEXT);
                serializer.text(text);
                serializer.endTag(NAMESPACE, ELEMENT_TEXT);
            }
            if (url != null) {
                serializer.startTag(NAMESPACE, ELEMENT_URL);
                serializer.text(url);
                serializer.endTag(NAMESPACE, ELEMENT_URL);
            }
            serializer.endTag(NAMESPACE, ELEMENT_ENTRY);
            serializer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private boolean valid() {
        return user != null && (text != null || url != null);
    }

    private static @NonNull PublishedEntry readEntry(XmlPullParser parser, String id) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, ELEMENT_ENTRY);
        final PublishedEntry.Builder builder = new PublishedEntry.Builder();
        builder.id(id);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (name.equals(ELEMENT_USER)) {
                builder.user(Xml.readText(parser));
            } else if (name.equals(ELEMENT_URL)) {
                builder.url(Xml.readText(parser));
            } else if (name.equals(ELEMENT_TEXT)) {
                builder.text(Xml.readText(parser));
            } else {
                Xml.skip(parser);
            }
        }
        return builder.build();
    }

    private static class Builder {
        String id;
        String user;
        String text;
        String url;

        Builder id(String id) {
            this.id = id;
            return this;
        }

        Builder user(String user) {
            this.user = user;
            return this;
        }

        Builder text(String text) {
            this.text = text;
            return this;
        }

        Builder url(String url) {
            this.id = id;
            return this;
        }

        PublishedEntry build() {
            return new PublishedEntry(id, user, text, url);
        }
    }

}
