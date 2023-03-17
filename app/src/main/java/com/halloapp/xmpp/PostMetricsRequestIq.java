package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostMetricsRequest;

public class PostMetricsRequestIq extends HalloIq {

    private final String postId;

    public PostMetricsRequestIq(@NonNull String postId) {
        this.postId = postId;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setPostMetricsRequest(PostMetricsRequest.newBuilder()
                        .setPostId(postId).build());
    }
}

