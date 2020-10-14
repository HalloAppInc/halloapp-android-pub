package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.id.GroupId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Xml;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class GroupFeedMessageElement implements ExtensionElement {

    public static final String NAMESPACE = "halloapp:group:feed";
    public static final String ELEMENT = "group_feed";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT, Action.SHARE})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
        int SHARE = 2;
    }

    public final GroupId groupId;

    public final @Action int action;

    public final List<FeedItem> feedItemList = new ArrayList<>();

    public GroupFeedMessageElement(@NonNull GroupId groupId, @Action int action, List<FeedItem> feedItems) {
        this.groupId = groupId;
        this.action = action;
        this.feedItemList.addAll(feedItems);
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        return null;
    }

    public static class Provider extends ExtensionElementProvider<GroupFeedMessageElement> {

        @Override
        public GroupFeedMessageElement parse(XmlPullParser parser, int initialDepth) throws Exception {
            String actionStr = parser.getAttributeValue(null, "action");
            if (actionStr == null) {
                return null;
            }
            String gid = parser.getAttributeValue(null, "gid");
            GroupId groupId = GroupId.fromNullable(gid);
            if (groupId == null) {
                Log.e("GroupFeedMessageElement/parse gid is invalid");
                return null;
            }
            @Action int action;
            switch (actionStr) {
                case "publish": {
                    action = Action.PUBLISH;
                    break;
                }
                case "retract": {
                    action = Action.RETRACT;
                    break;
                }
                case "share": {
                    action = Action.SHARE;
                    break;
                }
                default: {
                    return null;
                }
            }
            ArrayList<FeedItem> feedItems = new ArrayList<>();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                FeedItem feedItem = FeedItem.parseGroupFeedItem(parser);
                if (feedItem == null) {
                    Xml.skip(parser);
                } else {
                    feedItems.add(feedItem);
                }
            }
            if (feedItems.isEmpty()) {
                return null;
            }
            return new GroupFeedMessageElement(groupId, action, feedItems);
        }
    }
}
