package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PostMetricsResult;

public class PostMetricsResultIq extends HalloIq {

    public boolean success;
    public int impressions;

    public PostMetricsResultIq(@NonNull PostMetricsResult postMetricsResult) {
        success = postMetricsResult.getResult().equals(PostMetricsResult.Result.OK);
        impressions = postMetricsResult.getPostMetrics().getNumImpressions();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static PostMetricsResultIq fromProto(@NonNull PostMetricsResult postMetricsResult) {
        return new PostMetricsResultIq(postMetricsResult);
    }
}

