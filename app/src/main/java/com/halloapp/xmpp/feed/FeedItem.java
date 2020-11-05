package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Xml;
import com.halloapp.util.logs.Log;

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
    public String parentCommentId;
    public String publisherId;
    public String publisherName;
    public Long timestamp;

    public final @Nullable String payload;

    public FeedItem(@Type int type, @NonNull String postId, @Nullable String payload) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.parentPostId = null;
        this.parentCommentId = null;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @Nullable String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.parentPostId = parentPostId;
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
        String parentCommentId = parser.getAttributeValue(null, "parent_comment_id");

        String id = parser.getAttributeValue(null, "id");
        if (id == null) {
            Log.e("FeedItem/parsePostFeedItem no id set");
            return null;
        }

        String payload = Xml.readText(parser);
        
        FeedItem comment = new FeedItem(Type.COMMENT, id, postId, payload);
        comment.publisherName = publisherName;
        comment.publisherId = publisherId;
        comment.parentCommentId = parentCommentId;
        if (timestampStr != null) {
            comment.timestamp = Long.parseLong(timestampStr);
        }
        return comment;
    }
}
