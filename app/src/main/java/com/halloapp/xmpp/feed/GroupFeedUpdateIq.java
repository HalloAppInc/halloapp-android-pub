package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.privacy.PrivacyList;

import org.jivesoftware.smack.packet.IQ;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupFeedUpdateIq extends IQ {

    public static final String ELEMENT = "group_feed";
    public static final String NAMESPACE = "halloapp:group:feed";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_GID = "gid";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
    }

    private @NonNull GroupId groupId;
    private @Action int action;
    private @NonNull FeedItem feedItem;

    public GroupFeedUpdateIq(@NonNull GroupId groupId, @Action int action, @NonNull FeedItem feedItem) {
        super(ELEMENT, NAMESPACE);

        setType(Type.set);

        this.groupId = groupId;
        this.action = action;
        this.feedItem = feedItem;
    }

    private String getActionString() {
        switch (action) {
            case Action.PUBLISH:
                return "publish";
            case Action.RETRACT:
                return "retract";
        }
        return null;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute(ATTRIBUTE_ACTION, getActionString());
        xml.attribute(ATTRIBUTE_GID, groupId.rawId());
        xml.rightAngleBracket();
        feedItem.toNode(xml);
        return xml;
    }
}
