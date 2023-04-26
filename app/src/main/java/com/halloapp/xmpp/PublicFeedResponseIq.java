package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PublicFeedItem;
import com.halloapp.proto.server.PublicFeedResponse;

import java.util.List;

public class PublicFeedResponseIq extends HalloIq {

    public boolean success;
    public String cursor;
    public boolean restarted;
    public List<PublicFeedItem> items;
    public List<String> geotags;

    private PublicFeedResponseIq(@NonNull PublicFeedResponse publicFeedResponse) {
        this.success = publicFeedResponse.getResult().equals(PublicFeedResponse.Result.SUCCESS);
        this.cursor = publicFeedResponse.getCursor();
        this.restarted = publicFeedResponse.getCursorRestarted();
        this.items = publicFeedResponse.getItemsList();
        this.geotags = publicFeedResponse.getGeoTagsList();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static PublicFeedResponseIq fromProto(@NonNull PublicFeedResponse publicFeedResponse) {
        return new PublicFeedResponseIq(publicFeedResponse);
    }
}
