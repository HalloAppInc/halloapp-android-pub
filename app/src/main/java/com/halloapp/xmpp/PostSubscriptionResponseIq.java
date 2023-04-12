package com.halloapp.xmpp;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.proto.server.FeedItem;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostSubscriptionResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class PostSubscriptionResponseIq extends HalloIq {

    public boolean success;
    public @Reason int reason;
    public List<FeedItem> feedItems;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Reason.UNKNOWN_REASON, Reason.INVALID_POST_ID})
    public @interface Reason {
        int UNKNOWN_REASON = -1;
        int INVALID_POST_ID = 0;
    }

    public PostSubscriptionResponseIq(@NonNull PostSubscriptionResponse postSubscriptionResponse) {
        this.success = postSubscriptionResponse.getResult().equals(PostSubscriptionResponse.Result.SUCCESS);
        this.reason = parseReason(postSubscriptionResponse.getReason());
        this.feedItems = postSubscriptionResponse.getItemsList();
    }

    private static @Reason int parseReason(PostSubscriptionResponse.Reason reason) {
        if (reason == PostSubscriptionResponse.Reason.INVALID_POST_ID) {
            return Reason.INVALID_POST_ID;
        }
        return Reason.UNKNOWN_REASON;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static PostSubscriptionResponseIq fromProto(@NonNull PostSubscriptionResponse postSubscriptionResponse) {
        return new PostSubscriptionResponseIq(postSubscriptionResponse);
    }
}
