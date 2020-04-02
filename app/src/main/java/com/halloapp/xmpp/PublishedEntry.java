package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.proto.ChatMessage;
import com.halloapp.proto.Comment;
import com.halloapp.proto.Container;
import com.halloapp.proto.MediaType;
import com.halloapp.proto.Post;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.NamedElement;
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

    private static final String ELEMENT = "entry";
    private static final String NAMESPACE = "http://halloapp.com/published-entry";

    private static final String ELEMENT_FEED_POST = "feedpost";
    private static final String ELEMENT_COMMENT = "comment";
    private static final String ELEMENT_CHAT_MESSAGE = "chatmessage";
    private static final String ELEMENT_TEXT = "text";
    private static final String ELEMENT_FEED_ITEM_ID = "feedItemId";
    private static final String ELEMENT_PARENT_COMMENT_ID = "parentCommentId";
    private static final String ELEMENT_URL = "url";
    private static final String ELEMENT_MEDIA = "media";
    private static final String ELEMENT_PROTOBUF_STAGE_ONE = "s1";

    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_WIDTH = "width";
    private static final String ATTRIBUTE_HEIGHT = "height";
    private static final String ATTRIBUTE_ENC_KEY = "key";
    private static final String ATTRIBUTE_SHA256_HASH = "sha256hash";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENTRY_FEED, ENTRY_COMMENT, ENTRY_CHAT})
    @interface EntryType {}
    static final int ENTRY_FEED = 0;
    static final int ENTRY_COMMENT = 1;
    static final int ENTRY_CHAT = 2;

    final @EntryType int type;
    final String id;
    final long timestamp;
    final String user;
    final String text;
    final String feedItemId;
    final String parentCommentId;
    final List<Media> media = new ArrayList<>();

    public static class Media {

        @Retention(RetentionPolicy.SOURCE)
        @StringDef({
                MEDIA_TYPE_IMAGE,
                MEDIA_TYPE_VIDEO
        })
        @interface MediaType {}
        static final String MEDIA_TYPE_IMAGE = "image";
        static final String MEDIA_TYPE_VIDEO = "video";

        final String type;
        final String url;
        final byte [] encKey;
        final byte [] sha256hash;
        int width;
        int height;

        public Media(@MediaType String type, String url, byte [] encKey, byte [] sha256hash, int width, int height) {
            this.type = type;
            this.url = url;
            this.encKey = encKey;
            this.sha256hash = sha256hash;
            this.width = width;
            this.height = height;
        }

        Media(@MediaType String type, String url, String encKey, String sha256hash, String widthText, String heightText) {
            this.type = type;
            this.url = url;

            this.encKey = encKey == null ? null : Base64.decode(encKey, Base64.NO_WRAP);
            this.sha256hash = sha256hash == null ? null : Base64.decode(sha256hash, Base64.NO_WRAP);

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

    static @com.halloapp.content.Media.MediaType int getMediaType(@PublishedEntry.Media.MediaType String protocolMediaType) {
        switch (protocolMediaType) {
            case PublishedEntry.Media.MEDIA_TYPE_IMAGE: {
                return com.halloapp.content.Media.MEDIA_TYPE_IMAGE;
            }
            case PublishedEntry.Media.MEDIA_TYPE_VIDEO: {
                return com.halloapp.content.Media.MEDIA_TYPE_VIDEO;
            }
            default: {
                return com.halloapp.content.Media.MEDIA_TYPE_UNKNOWN;
            }
        }
    }

    static @PublishedEntry.Media.MediaType String getMediaType(@com.halloapp.content.Media.MediaType int mediaType) {
        switch (mediaType) {
            case com.halloapp.content.Media.MEDIA_TYPE_IMAGE: {
                return PublishedEntry.Media.MEDIA_TYPE_IMAGE;
            }
            case com.halloapp.content.Media.MEDIA_TYPE_VIDEO: {
                return PublishedEntry.Media.MEDIA_TYPE_VIDEO;
            }
            case com.halloapp.content.Media.MEDIA_TYPE_UNKNOWN:
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    static @NonNull List<PublishedEntry> getEntries(@NonNull List<? extends NamedElement> items) {
        final List<PublishedEntry> entries = new ArrayList<>();
        for (NamedElement item : items) {
            if (item instanceof PubSubItem) {
                final PublishedEntry entry = getEntry((PubSubItem)item);
                if (entry != null && entry.valid()) {
                    entries.add(entry);
                }
            } else {
                Log.e("PublishedEntry.getEntries: unknown feed entry " + item);
            }
        }
        return entries;
    }

    private static @Nullable PublishedEntry getEntry(@NonNull PubSubItem item) {
        final String xml = item.getPayload().toXML(null);
        final XmlPullParser parser = android.util.Xml.newPullParser();
        PublishedEntry.Builder entryBuilder = null;
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xml));
            parser.nextTag();
            entryBuilder = readEntry(parser);
        } catch (XmlPullParserException | IOException e) {
            Log.e("PublishedEntry.getEntry", e);
        }
        if (entryBuilder != null) {
            entryBuilder.id(item.getId());
            entryBuilder.timestamp(item.getTimestamp());
            if (item.getPublisher() != null) {
                entryBuilder.user(item.getPublisher().getLocalpartOrNull().toString());
            }
        }
        return entryBuilder == null ? null : entryBuilder.build();
    }

    public static @NonNull PublishedEntry.Builder readEntry(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, ELEMENT);
        PublishedEntry.Builder entryBuilder = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (name.equals(ELEMENT_PROTOBUF_STAGE_ONE)) {
                entryBuilder = readEncodedEntry(Xml.readText(parser));

                // Stage one takes precedence over the other tags
                return Preconditions.checkNotNull(entryBuilder);
            } else if (name.equals(ELEMENT_FEED_POST)) {
                entryBuilder = readEntryContent(parser);
                entryBuilder.type(ENTRY_FEED);
            } else if (name.equals(ELEMENT_COMMENT)) {
                entryBuilder = readEntryContent(parser);
                entryBuilder.type(ENTRY_COMMENT);
            } else if (name.equals(ELEMENT_CHAT_MESSAGE)) {
                entryBuilder = readEntryContent(parser);
                entryBuilder.type(ENTRY_CHAT);
            } else {
                Xml.skip(parser);
            }
        }
        return Preconditions.checkNotNull(entryBuilder);
    }

    private static @NonNull PublishedEntry.Builder readEntryContent(@NonNull XmlPullParser parser) throws XmlPullParserException, IOException {
        final PublishedEntry.Builder builder = new PublishedEntry.Builder();
        final List<Media> media = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (ELEMENT_MEDIA.equals(name)) {
                media.clear();
                media.addAll(readMedia(parser));
            } else if (ELEMENT_TEXT.equals(name)) {
                builder.text(Xml.readText(parser));
            } else if (ELEMENT_FEED_ITEM_ID.equals(name)) {
                builder.feedItemId(Xml.readText(parser));
            } else if (ELEMENT_PARENT_COMMENT_ID.equals(name)) {
                builder.parentCommentId(Xml.readText(parser));
            } else {
                Xml.skip(parser);
            }
        }
        builder.media(media);
        return builder;
    }

    private static List<Media> readMedia(XmlPullParser parser) throws IOException, XmlPullParserException {
        final List<Media> media = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String name = Preconditions.checkNotNull(parser.getName());
            if (ELEMENT_URL.equals(name)) {
                final String type = parser.getAttributeValue(null, ATTRIBUTE_TYPE);
                final String widthStr = parser.getAttributeValue(null, ATTRIBUTE_WIDTH);
                final String heightStr = parser.getAttributeValue(null, ATTRIBUTE_HEIGHT);
                final String key = parser.getAttributeValue(null, ATTRIBUTE_ENC_KEY);
                final String sha256hash = parser.getAttributeValue(null, ATTRIBUTE_SHA256_HASH);
                final Media mediaItem = new Media(type, Xml.readText(parser),
                        key, sha256hash, widthStr, heightStr);
                if (!TextUtils.isEmpty(mediaItem.url)) {
                    media.add(mediaItem);
                }
            } else {
                Xml.skip(parser);
            }
        }
        return media;
    }


    PublishedEntry(@EntryType int type, String id, long timestamp, String user, String text, String feedItemId, String parentCommentId) {
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        if (text != null && text.length() > Constants.MAX_TEXT_LENGTH) {
            this.text = text.substring(0, Constants.MAX_TEXT_LENGTH);
        } else {
            this.text = text;
        }
        this.feedItemId = feedItemId;
        this.parentCommentId = parentCommentId;
    }

    @NonNull String toXml() {
        final XmlSerializer serializer = android.util.Xml.newSerializer();
        final StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.setPrefix("", NAMESPACE);
            serializer.startTag(NAMESPACE, ELEMENT);
            serializer.startTag(NAMESPACE, getTag());
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
                    if (mediaItem.encKey != null) {
                        serializer.attribute(null, ATTRIBUTE_ENC_KEY, Base64.encodeToString(mediaItem.encKey, Base64.NO_WRAP));
                    }
                    if (mediaItem.sha256hash != null) {
                        serializer.attribute(null, ATTRIBUTE_SHA256_HASH, Base64.encodeToString(mediaItem.sha256hash, Base64.NO_WRAP));
                    }
                    serializer.text(mediaItem.url);
                    serializer.endTag(NAMESPACE, ELEMENT_URL);
                }
                serializer.endTag(NAMESPACE, ELEMENT_MEDIA);

            }
            serializer.endTag(NAMESPACE, getTag());

            serializer.startTag(NAMESPACE, ELEMENT_PROTOBUF_STAGE_ONE);
            serializer.text(getEncodedEntry());
            serializer.endTag(NAMESPACE, ELEMENT_PROTOBUF_STAGE_ONE);

            serializer.endTag(NAMESPACE, ELEMENT);
            serializer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private String getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        switch (type) {
            case ENTRY_FEED: {
                Post.Builder postBuilder = Post.newBuilder();
                if (!media.isEmpty()) {
                    postBuilder.addAllMedia(getMediaProtos());
                }
                postBuilder.setText(text);
                containerBuilder.setPost(postBuilder.build());
                break;
            }
            case ENTRY_COMMENT: {
                Comment.Builder commentBuilder = Comment.newBuilder();
                commentBuilder.setFeedPostId(feedItemId);
                commentBuilder.setSenderUserId(user);
                if (parentCommentId != null) {
                    commentBuilder.setParentCommentId(parentCommentId);
                }
                commentBuilder.setText(text);
                containerBuilder.setComment(commentBuilder.build());
                break;
            }
            case ENTRY_CHAT: {
                ChatMessage.Builder chatMessageBuilder = ChatMessage.newBuilder();
                if (!media.isEmpty()) {
                    chatMessageBuilder.addAllMedia(getMediaProtos());
                }
                chatMessageBuilder.setText(text);
                containerBuilder.setChatMessage(chatMessageBuilder.build());
                break;
            }
            default: {
                throw new IllegalStateException("Unknown type " + type);
            }
        }
        return Base64.encodeToString(containerBuilder.build().toByteArray(), Base64.DEFAULT);
    }

    private List<com.halloapp.proto.Media> getMediaProtos() {
        Preconditions.checkState(!media.isEmpty(), "Trying to get empty media proto");

        List<com.halloapp.proto.Media> mediaList = new ArrayList<>();
        for (Media item : media) {
            com.halloapp.proto.Media.Builder mediaBuilder = com.halloapp.proto.Media.newBuilder();
            mediaBuilder.setType(getProtoMediaType(item.type));
            mediaBuilder.setWidth(item.width);
            mediaBuilder.setHeight(item.height);
            mediaBuilder.setEncryptionKey(ByteString.copyFrom(item.encKey));
            mediaBuilder.setPlaintextHash(ByteString.copyFrom(item.sha256hash));
            mediaBuilder.setDownloadUrl(item.url);
            mediaList.add(mediaBuilder.build());
        }
        return mediaList;
    }

    private MediaType getProtoMediaType(@NonNull String s) {
        if ("image".equals(s)) {
            return MediaType.MEDIA_TYPE_IMAGE;
        } else if ("video".equals(s)) {
            return MediaType.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized media type string " + s);
        return MediaType.MEDIA_TYPE_UNSPECIFIED;
    }

    private static String fromProtoMediaType(@NonNull MediaType type) {
        if (type == MediaType.MEDIA_TYPE_IMAGE) {
            return Media.MEDIA_TYPE_IMAGE;
        } else if (type == MediaType.MEDIA_TYPE_VIDEO) {
            return Media.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized MediaType " + type);
        return null;
    }

    private static List<Media> fromMediaProtos(List<com.halloapp.proto.Media> mediaList) {
        List<Media> ret = new ArrayList<>();
        for (com.halloapp.proto.Media item : mediaList) {
            ret.add(new Media(fromProtoMediaType(
                    item.getType()),
                    item.getDownloadUrl(),
                    item.getEncryptionKey().toByteArray(),
                    item.getPlaintextHash().toByteArray(),
                    item.getWidth(),
                    item.getHeight()));
        }
        return ret;
    }

    private static Builder readEncodedEntry(String entry) {
        try {
            Container container = Container.parseFrom(Base64.decode(entry, Base64.DEFAULT));
            Builder builder = new Builder();

            if (container.hasPost()) {
                Post post = container.getPost();
                builder.type(ENTRY_FEED);
                builder.text(post.getText());
                builder.media(fromMediaProtos(post.getMediaList()));
            } else if (container.hasComment()) {
                Comment comment = container.getComment();
                builder.type(ENTRY_COMMENT);
                builder.feedItemId(comment.getFeedPostId());
                builder.user(comment.getSenderUserId());
                builder.parentCommentId(comment.getParentCommentId());
                builder.text(comment.getText());
            } else if (container.hasChatMessage()) {
                ChatMessage chatMessage = container.getChatMessage();
                builder.type(ENTRY_CHAT);
                builder.text(chatMessage.getText());
                builder.media(fromMediaProtos(chatMessage.getMediaList()));
            } else {
                Log.i("Unknown encoded entry type");
            }

            return builder;
        } catch (InvalidProtocolBufferException e) {
            Log.w("Error reading encoded entry", e);
        }
        return null;
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
            case ENTRY_CHAT: {
                return ELEMENT_CHAT_MESSAGE;
            }
            default: {
                throw new IllegalStateException("Unknown type " + type);
            }
        }
    }

    private boolean valid() {
        return true; // timestamp != 0 && user != null && (text != null || !media.isEmpty());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        @EntryType int type;
        String id;
        long timestamp;
        String user;
        String text;
        String feedItemId;
        String parentCommentId;
        List<Media> media;

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

        Builder timestamp(long timestampSeconds) {
            timestamp = 1000L * timestampSeconds;
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

        Builder media(List<Media> media) {
            this.media = media;
            return this;
        }

        PublishedEntry build() {
            final PublishedEntry entry = new PublishedEntry(type, id, timestamp, user, text, feedItemId, parentCommentId);
            if (media != null) {
                entry.media.addAll(media);
            }
            return entry;
        }
    }
}
