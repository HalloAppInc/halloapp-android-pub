package com.halloapp.xmpp.feed;

import androidx.annotation.NonNull;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.ShareStanza;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class SharePosts {

    private final UserId userIdToShareTo;

    public final List<FeedItem> postsToShare = new ArrayList<>();

    public SharePosts(@NonNull UserId userIdToShareTo, List<FeedItem> postsToShare) {
        this.userIdToShareTo = userIdToShareTo;
        this.postsToShare.addAll(postsToShare);
    }

    public ShareStanza toProto() {
        ShareStanza.Builder builder = ShareStanza.newBuilder();
        builder.setUid(Long.parseLong(userIdToShareTo.rawId()));
        for (FeedItem item : postsToShare) {
            if (item.type != FeedItem.Type.POST) {
                Log.d("SharePosts/toNode attempting to share non-post");
                continue;
            }
            builder.addPostIds(item.id);
        }
        return builder.build();
    }
}
