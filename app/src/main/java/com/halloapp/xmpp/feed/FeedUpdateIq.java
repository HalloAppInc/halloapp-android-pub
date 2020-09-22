package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.xmpp.privacy.PrivacyList;

import org.jivesoftware.smack.packet.IQ;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class FeedUpdateIq extends IQ {

    public static final String ELEMENT = "feed";
    public static final String NAMESPACE = "halloapp:feed";

    private static final String ATTRIBUTE_ACTION = "action";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT, Action.SHARE})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
        int SHARE = 2;
    }

    private @Action int action;
    private @Nullable FeedItem feedItem;

    private @Nullable @PrivacyList.Type String audienceType;
    private @Nullable List<UserId> audienceList;

    private @Nullable SharePosts sharePosts;

    public FeedUpdateIq(@Action int action, @NonNull FeedItem feedItem) {
        super(ELEMENT, NAMESPACE);

        setType(Type.set);

        this.action = action;
        this.feedItem = feedItem;
    }

    public FeedUpdateIq(@NonNull SharePosts sharePosts) {
        super(ELEMENT, NAMESPACE);
        this.action = Action.SHARE;

        setType(Type.set);

        this.sharePosts = sharePosts;
    }

    private String getActionString() {
        switch (action) {
            case Action.PUBLISH:
                return "publish";
            case Action.RETRACT:
                return "retract";
            case Action.SHARE:
                return "share";
        }
        return null;
    }

    public void setPostAudience(@PrivacyList.Type String audienceType, List<UserId> audienceList) {
        this.audienceType = audienceType;
        this.audienceList = audienceList;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, getActionString());
        xml.rightAngleBracket();
        if (action == Action.SHARE && sharePosts != null) {
            sharePosts.toNode(xml);
        } else {
            feedItem.toNode(xml);
            if (audienceType != null && audienceList != null) {
                xml.halfOpenElement("audience_list");
                xml.attribute("type", audienceType);
                xml.rightAngleBracket();
                for (UserId user : audienceList) {
                    xml.openElement("uid");
                    xml.append(user.rawId());
                    xml.closeElement("uid");
                }
                xml.closeElement("audience_list");
            }
        }
        return xml;
    }
}
