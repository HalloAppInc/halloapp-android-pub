package com.halloapp.protocol;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;

import com.halloapp.util.Log;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class PublishedEntry {

    public static final String ELEMENT_ENTRY = "entry";
    public static final String ELEMENT_FEED_POST = "feedpost";
    public static final String ELEMENT_COMMENT = "comment";
    public static final String ELEMENT_USER = "username";
    public static final String ELEMENT_URL = "imageUrl";
    public static final String ELEMENT_TEXT = "text";
    public static final String ELEMENT_TIMESTAMP = "timestamp";
    public static final String ELEMENT_FEED_ITEM_ID = "feedItemId";

    private static final String NAMESPACE = "http://halloapp.com/published-entry";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENTRY_FEED, ENTRY_COMMENT})
    public @interface EntryType {}
    public static final int ENTRY_FEED = 0;
    public static final int ENTRY_COMMENT = 1;

    public final @EntryType int type;
    public final String id;
    public final long timestamp;
    public final String user;
    public final String text;
    public final String url;
    public final String feedItemId;

    public PublishedEntry(@EntryType int type, String id, long timestamp, String user, String text, String url, String feedItemId) {
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.text = text;
        this.url = url;
        this.feedItemId = feedItemId;
    }

    public static @NonNull List<PublishedEntry> getPublishedItems(@NonNull List<PayloadItem<SimplePayload>> items) {
        final List<PublishedEntry> entries = new ArrayList<>();
        for (PayloadItem<SimplePayload> item : items) {
            final String xml = item.getPayload().toXML(null);
            final XmlPullParser parser = android.util.Xml.newPullParser();
            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));
                parser.nextTag();
                parser.require(XmlPullParser.START_TAG, null, ELEMENT_ENTRY);
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    final String name = Preconditions.checkNotNull(parser.getName());
                    PublishedEntry entry = null;
                    if (name.equals(ELEMENT_FEED_POST)) {
                        entry = readEntry(parser, ENTRY_FEED, item.getId());
                    } else if (name.equals(ELEMENT_COMMENT)) {
                        entry = readEntry(parser, ENTRY_COMMENT, item.getId());
                    } else {
                        Xml.skip(parser);
                    }
                    if (entry != null && entry.valid()) {
                        entries.add(entry);
                    }
                }
            } catch (XmlPullParserException | IOException e) {
                Log.e("PublishedEntry.getPublishedItems", e);
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
            serializer.startTag(NAMESPACE, getTag());
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
            serializer.startTag(NAMESPACE, ELEMENT_TIMESTAMP); // TODO (ds): remove; should be set on server
            serializer.text(Long.toString(timestamp / 1000));
            serializer.endTag(NAMESPACE, ELEMENT_TIMESTAMP);
            serializer.endTag(NAMESPACE, getTag());
            serializer.endTag(NAMESPACE, ELEMENT_ENTRY);
            serializer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public @NonNull String getTag() {
        switch (type) {
            case ENTRY_FEED: {
                return ELEMENT_FEED_POST;
            }
            case ENTRY_COMMENT: {
                return ELEMENT_COMMENT;
            }
            default: {
                throw new IllegalStateException("Unknown type " + type);
            }
        }
    }

    private boolean valid() {
        return user != null && (text != null || url != null);
    }

    private static @NonNull PublishedEntry readEntry(XmlPullParser parser, @EntryType int type, String id) throws XmlPullParserException, IOException {
        final PublishedEntry.Builder builder = new PublishedEntry.Builder();
        builder.type(type);
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
            } else if (name.equals(ELEMENT_FEED_ITEM_ID)) {
                builder.feedItemId(Xml.readText(parser));
            } else if (name.equals(ELEMENT_TIMESTAMP)) {
                final String timestampText = Xml.readText(parser);
                long timestampSeconds;
                try {
                    timestampSeconds = Long.parseLong(timestampText);
                } catch (NumberFormatException e) {
                    try {
                        timestampSeconds = (long)Double.parseDouble(timestampText);
                    } catch (NumberFormatException ex) {
                        Log.e("PublishedEntry: failed reading timestamp", ex);
                        timestampSeconds = System.currentTimeMillis() / 1000;
                    }
                }
                builder.timestamp(timestampSeconds * 1000L);
            } else {
                Xml.skip(parser);
            }
        }
        return builder.build();
    }

    private static class Builder {
        @EntryType int type;
        String id;
        long timestamp;
        String user;
        String text;
        String url;
        String feedItemId;

        Builder type(@EntryType int type) {
            this.type = type;
            return this;
        }

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

        Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        Builder feedItemId(String feedItemId) {
            this.feedItemId = feedItemId;
            return this;
        }

        Builder url(String url) {
            if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
                url = "https://cdn.image4.io/hallo" + url; // TODO (ds): remove
            }
            this.url = url;
            return this;
        }

        PublishedEntry build() {
            return new PublishedEntry(type, id, timestamp, user, text, url, feedItemId);
        }
    }

}
