package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;

import org.jivesoftware.smack.packet.IQ;

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
    public final UserId parentPostSenderId;

    public final @Nullable String payload;

    public FeedItem(@Type int type, @NonNull String postId, @Nullable String payload) {
        this.id = postId;
        this.type = type;
        this.payload = payload;
        this.parentPostId = null;
        this.parentPostSenderId = null;
    }

    public FeedItem(@Type int type, @NonNull String id, @NonNull String parentPostId, @NonNull UserId parentPostSenderId, @Nullable String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
        this.parentPostId = parentPostId;
        this.parentPostSenderId = parentPostSenderId;
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
            builder.attribute("post_uid", parentPostSenderId.rawId());
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
