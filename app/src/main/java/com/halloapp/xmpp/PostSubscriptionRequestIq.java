package com.halloapp.xmpp;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostSubscriptionRequest;

public class PostSubscriptionRequestIq extends HalloIq {

    public String postId;
    public PostSubscriptionRequest.Action action;

    public PostSubscriptionRequestIq(String postId, PostSubscriptionRequest.Action action) {
        this.postId = postId;
        this.action = action;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setPostSubscriptionRequest(PostSubscriptionRequest.newBuilder()
                        .setPostId(postId)
                        .setAction(action)
                        .build());
    }

}
