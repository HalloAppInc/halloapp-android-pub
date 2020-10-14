package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FeedItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.POST, Type.COMMENT})
    public @interface Type {
        int POST = 0;
        int COMMENT = 1;
    }

    public final @Type int type;
    public final @NonNull String id;
    public final String parentPostId;
    public final String parentPostSenderId;
    public String publisherId;
    public String publisherName;
    public Long timestamp;

    public final @Nullable String payload;

    public FeedItem(@Type int type, @NonNull String postId, @Nullable String payload) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.parentPostId = null;
        this.parentPostSenderId = null;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @NonNull String parentPostSenderId, @Nullable String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.parentPostId = parentPostId;
        this.parentPostSenderId = parentPostSenderId;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable UserId parentPostSenderId, @Nullable String payload) {
        this(type, id, parentPostId, parentPostSenderId == null ? null : parentPostSenderId.rawId(), payload);
    }

    @Nullable
    public static FeedItem parseFeedItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        switch (parser.getName()) {
            case "post":
                return parsePostFeedItem(parser);
            case "comment":
                return parseCommentFeedItem(parser);
            default:
                return null;
        }
    }

    @Nullable
    public static FeedItem parseGroupFeedItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        switch (parser.getName()) {
            case "post":
                return parseGroupPostFeedItem(parser);
            case "comment":
                return parseCommentFeedItem(parser);
            default:
                return null;
        }
    }

    @Nullable
    public static FeedItem parseGroupPostFeedItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        String timestampStr = parser.getAttributeValue(null, "timestamp");
        String uid = parser.getAttributeValue(null, "publisher_uid");
        String id = parser.getAttributeValue(null, "id");
        if (id == null) {
            Log.e("FeedItem/parsePostFeedItem no id set");
            return null;
        }
        String payload = Xml.readText(parser);
        FeedItem post = new FeedItem(Type.POST, id, payload);
        post.publisherId = uid;
        if (timestampStr != null) {
            post.timestamp = Long.parseLong(timestampStr);
        }
        return post;
    }

    @Nullable
    public static FeedItem parsePostFeedItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        String timestampStr = parser.getAttributeValue(null, "timestamp");
        String uid = parser.getAttributeValue(null, "uid");
        String id = parser.getAttributeValue(null, "id");
        if (id == null) {
            Log.e("FeedItem/parsePostFeedItem no id set");
            return null;
        }
        String payload = Xml.readText(parser);
        FeedItem post = new FeedItem(Type.POST, id, payload);
        post.publisherId = uid;
        if (timestampStr != null) {
            post.timestamp = Long.parseLong(timestampStr);
        }
        return post;
    }

    @Nullable
    public static FeedItem parseCommentFeedItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        String timestampStr = parser.getAttributeValue(null, "timestamp");
        String publisherId = parser.getAttributeValue(null, "publisher_uid");
        String publisherName = parser.getAttributeValue(null, "publisher_name");
        String postId = parser.getAttributeValue(null, "post_id");
        String postUid = parser.getAttributeValue(null, "post_uid");

        String id = parser.getAttributeValue(null, "id");
        if (id == null) {
            Log.e("FeedItem/parsePostFeedItem no id set");
            return null;
        }

        String payload = Xml.readText(parser);
        
        FeedItem comment = new FeedItem(Type.COMMENT, id, postId, postUid, payload);
        comment.publisherName = publisherName;
        comment.publisherId = publisherId;
        if (timestampStr != null) {
            comment.timestamp = Long.parseLong(timestampStr);
        }
        return comment;
    }

    private String getType() {
        switch (type) {
            case Type.POST:
                return "post";
            case Type.COMMENT:
                return "comment";
        }
        return "post";
    }

    public IQ.IQChildElementXmlStringBuilder toNode(IQ.IQChildElementXmlStringBuilder builder) {
        String elementName = getType();
        builder.halfOpenElement(elementName);
        builder.attribute("id", id);
        if (parentPostSenderId != null && parentPostId != null) {
            builder.attribute("post_id", parentPostId);
            builder.attribute("post_uid", parentPostSenderId);
        }
        if (payload == null) {
            builder.closeEmptyElement();
        } else {
            builder.rightAngleBracket();
            builder.append(payload);
            builder.closeElement(elementName);
        }
        return builder;
    }
}
