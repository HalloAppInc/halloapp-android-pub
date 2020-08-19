package com.halloapp.xmpp.feed;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.util.Log;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

public class SharePosts {

    private UserId userIdToShareTo;

    public final List<FeedItem> postsToShare = new ArrayList<>();

    public SharePosts(@NonNull UserId userIdToShareTo, List<FeedItem> postsToShare) {
        this.userIdToShareTo = userIdToShareTo;
        this.postsToShare.addAll(postsToShare);
    }

    public IQ.IQChildElementXmlStringBuilder toNode(IQ.IQChildElementXmlStringBuilder builder) {
        String elementName = "share_posts";
        builder.halfOpenElement(elementName);
        builder.attribute("uid", userIdToShareTo.rawId());
        builder.rightAngleBracket();
        for (FeedItem item : postsToShare) {
            if (item.type != FeedItem.Type.POST) {
                Log.d("SharePosts/toNode attempting to share non-post");
                continue;
            }
            item.toNode(builder);
        }
        builder.closeElement(elementName);
        return builder;
    }

}
