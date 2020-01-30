package com.halloapp.protocol;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.core.util.Preconditions;

import com.halloapp.util.Log;
import com.halloapp.util.Xml;

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

    private static final String ELEMENT_ENTRY = "entry";
    private static final String ELEMENT_FEED_POST = "feedpost";
    private static final String ELEMENT_COMMENT = "comment";
    private static final String ELEMENT_USER = "username";
    private static final String ELEMENT_IMAGE_URL = "imageUrl";
    private static final String ELEMENT_TEXT = "text";
    private static final String ELEMENT_TIMESTAMP = "timestamp";
    private static final String ELEMENT_FEED_ITEM_ID = "feedItemId";
    private static final String ELEMENT_PARENT_COMMENT_ID = "parentCommentId";
    private static final String ELEMENT_URL = "url";
    private static final String ELEMENT_MEDIA = "media";

    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_WIDTH = "width";
    private static final String ATTRIBUTE_HEIGHT = "height";

    private static final String NAMESPACE = "http://halloapp.com/published-entry";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENTRY_FEED, ENTRY_COMMENT})
    @interface EntryType {}
    public static final int ENTRY_FEED = 0;
    public static final int ENTRY_COMMENT = 1;

    public final @EntryType int type;
    public final String id;
    public final long timestamp;
    public final String user;
    public final String text;
    public final String feedItemId;
    public final String parentCommentId;
    public final List<Media> media = new ArrayList<>();

    public static class Media {

        @Retention(RetentionPolicy.SOURCE)
        @StringDef({
                MEDIA_TYPE_IMAGE,
                MEDIA_TYPE_VIDEO
        })
        public @interface MediaType {}
        public static final String MEDIA_TYPE_IMAGE = "image";
        public static final String MEDIA_TYPE_VIDEO = "video";

        public String type;
        public String url;
        public int width;
        public int height;

        public Media(@MediaType String type, String url, int width, int height) {
            this.type = type;
            this.url = url;
            this.width = width;
            this.height = height;
        }

        Media(@MediaType String type, String widthText, String heightText, String url) {
            if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
                this.url = "https://cdn.image4.io/hallo" + url; // TODO (ds): remove
            } else {
                this.url = url;
            }

            this.type = type;

            if (widthText != null) {
                try {
                    this.width = Integer.parseInt(widthText);
                } catch (NumberFormatException ex) {
                    Log.e("PublishedEntry: invalid width", ex);
                }
            }

            if (heightText != null) {
                try {
                    this.height = Integer.parseInt(heightText);
                } catch (NumberFormatException ex) {
                    Log.e("PublishedEntry: invalid height", ex);
                }
            }
        }
    }

    public PublishedEntry(@EntryType int type, String id, long timestamp, String user, String text, String feedItemId, String parentCommentId) {
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.text = text;
        this.feedItemId = feedItemId;
        this.parentCommentId = parentCommentId;
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
            if (feedItemId != null) {
                serializer.startTag(NAMESPACE, ELEMENT_FEED_ITEM_ID);
                serializer.text(feedItemId);
                serializer.endTag(NAMESPACE, ELEMENT_FEED_ITEM_ID);
            }
            if (parentCommentId != null) {
                serializer.startTag(NAMESPACE, ELEMENT_PARENT_COMMENT_ID);
                serializer.text(parentCommentId);
                serializer.endTag(NAMESPACE, ELEMENT_PARENT_COMMENT_ID);
            }
            if (!media.isEmpty()) {
                // TODO (ds): begin remove
                {
                    Media mediaItem = media.get(0);
                    serializer.startTag(NAMESPACE, ELEMENT_IMAGE_URL);
                    if (mediaItem.width != 0) {
                        serializer.attribute(null, ATTRIBUTE_WIDTH, Integer.toString(mediaItem.width));
                    }
                    if (mediaItem.height != 0) {
                        serializer.attribute(null, ATTRIBUTE_HEIGHT, Integer.toString(mediaItem.height));
                    }
                    serializer.text(mediaItem.url);
                    serializer.endTag(NAMESPACE, ELEMENT_IMAGE_URL);
                }
                // TODO (ds): end remove

                serializer.startTag(NAMESPACE, ELEMENT_MEDIA);
                for (Media mediaItem : media) {
                    serializer.startTag(NAMESPACE, ELEMENT_URL);
                    serializer.attribute(null, ATTRIBUTE_TYPE, mediaItem.type);
                    if (mediaItem.width != 0) {
                        serializer.attribute(null, ATTRIBUTE_WIDTH, Integer.toString(mediaItem.width));
                    }
                    if (mediaItem.height != 0) {
                        serializer.attribute(null, ATTRIBUTE_HEIGHT, Integer.toString(mediaItem.height));
                    }
                    serializer.text(mediaItem.url);
                    serializer.endTag(NAMESPACE, ELEMENT_URL);
                }
                serializer.endTag(NAMESPACE, ELEMENT_MEDIA);

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

    @Override
    public @NonNull String toString() {
        return "PublishedEntry[id=" + id + " type=" + type + " text=" + text + "]";
    }

    private @NonNull String getTag() {
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
        return user != null && (text != null || !media.isEmpty());
    }

    private static @NonNull PublishedEntry readEntry(XmlPullParser parser, @EntryType int type, String id) throws XmlPullParserException, IOException {
        final PublishedEntry.Builder builder = new PublishedEntry.Builder();
        builder.type(type);
        builder.id(id);
        final List<Media> media = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (ELEMENT_USER.equals(name)) {
                builder.user(Xml.readText(parser));
            } else if (ELEMENT_IMAGE_URL.equals(name)) { // TODO (ds): remove
                if (media.isEmpty()) {
                    final Media mediaItem = new Media(Media.MEDIA_TYPE_IMAGE,
                            parser.getAttributeValue(null, ATTRIBUTE_WIDTH),
                            parser.getAttributeValue(null, ATTRIBUTE_HEIGHT),
                            Xml.readText(parser));
                    if (!TextUtils.isEmpty(mediaItem.url)) {
                        media.add(mediaItem);
                    }
                }
            } else if (ELEMENT_MEDIA.equals(name)) {
                media.clear();
                media.addAll(readMedia(parser));
            } else if (ELEMENT_TEXT.equals(name)) {
                builder.text(Xml.readText(parser));
            } else if (ELEMENT_FEED_ITEM_ID.equals(name)) {
                builder.feedItemId(Xml.readText(parser));
            } else if (ELEMENT_PARENT_COMMENT_ID.equals(name)) {
                builder.parentCommentId(Xml.readText(parser));
            } else if (ELEMENT_TIMESTAMP.equals(name)) {
                builder.timestamp(Xml.readText(parser));
            } else {
                Xml.skip(parser);
            }
        }
        PublishedEntry entry = builder.build();
        entry.media.addAll(media);
        return entry;
    }

    private static List<Media> readMedia(XmlPullParser parser) throws IOException, XmlPullParserException {
        final List<Media> media = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (ELEMENT_URL.equals(name)) {
                media.add(new Media(
                        parser.getAttributeValue(null, ATTRIBUTE_TYPE),
                        parser.getAttributeValue(null, ATTRIBUTE_WIDTH),
                        parser.getAttributeValue(null, ATTRIBUTE_HEIGHT),
                        Xml.readText(parser)));
            } else {
                Xml.skip(parser);
            }
        }
        return media;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static class Builder {
        @EntryType int type;
        String id;
        long timestamp;
        String user;
        String text;
        String feedItemId;
        String parentCommentId;

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

        Builder timestamp(String timestampText) {
            try {
                this.timestamp = 1000L * Long.parseLong(timestampText);
            } catch (NumberFormatException e) {
                try {
                    this.timestamp = 1000L * (long)Double.parseDouble(timestampText);
                } catch (NumberFormatException ex) {
                    Log.e("PublishedEntry: failed reading timestamp", ex);
                    this.timestamp = System.currentTimeMillis() / 1000;
                }
            }
            return this;
        }

        Builder feedItemId(String feedItemId) {
            this.feedItemId = feedItemId;
            return this;
        }

        Builder parentCommentId(String parentCommentId) {
            this.parentCommentId = TextUtils.isEmpty(parentCommentId) ? null : parentCommentId;
            return this;
        }

        PublishedEntry build() {
            return new PublishedEntry(type, id, timestamp, user, text, feedItemId, parentCommentId);
        }
    }
}
