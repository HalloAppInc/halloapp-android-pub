package com.halloapp.xmpp.feed;

import androidx.annotation.IntDef;

import com.halloapp.Constants;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Xml;
import com.halloapp.xmpp.ChatMessageElement;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class FeedMessageElement implements ExtensionElement {

    public static final String NAMESPACE = "halloapp:feed";
    public static final String ELEMENT = "feed";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Action.PUBLISH, Action.RETRACT, Action.SHARE})
    public @interface Action {
        int PUBLISH = 0;
        int RETRACT = 1;
        int SHARE = 2;
    }

    public final @Action int action;

    public final List<FeedItem> feedItemList = new ArrayList<>();

    public FeedMessageElement(@Action int action, List<FeedItem> feedItems) {
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

    public static class Provider extends ExtensionElementProvider<FeedMessageElement> {

        @Override
        public FeedMessageElement parse(XmlPullParser parser, int initialDepth) throws Exception {
            if (!Constants.NEW_FEED_API) {
                return null;
            }
            String actionStr = parser.getAttributeValue(null, "action");
            if (actionStr == null) {
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
                FeedItem feedItem = FeedItem.parseFeedItem(parser);
                if (feedItem == null) {
                    Xml.skip(parser);
                } else {
                    feedItems.add(feedItem);
                }
            }
            if (feedItems.isEmpty()) {
                return null;
            }
            return new FeedMessageElement(action, feedItems);
        }
    }
}
