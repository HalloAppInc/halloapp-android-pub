package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.HalloappSearchResponse;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.Iq;

import java.util.List;

public class HalloappUserSearchResponseIq extends HalloIq {

    public boolean success;
    public List<HalloappUserProfile> profiles;

    public HalloappUserSearchResponseIq(@NonNull HalloappSearchResponse searchResponse) {
        success = searchResponse.getResult().equals(HalloappSearchResponse.Result.OK);
        profiles = searchResponse.getSearchResultList();
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static HalloappUserSearchResponseIq fromProto(@NonNull HalloappSearchResponse searchResponse) {
        return new HalloappUserSearchResponseIq(searchResponse);
    }
}
