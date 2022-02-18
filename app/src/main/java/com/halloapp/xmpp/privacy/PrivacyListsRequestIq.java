package com.halloapp.xmpp.privacy;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PrivacyLists;
import com.halloapp.xmpp.HalloIq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PrivacyListsRequestIq extends HalloIq {

    private final List<String> requestedTypes = new ArrayList<>();

    protected PrivacyListsRequestIq(@PrivacyList.Type String... types) {
        requestedTypes.addAll(Arrays.asList(types));
    }

    @Override
    public Iq.Builder toProtoIq() {
        PrivacyLists.Builder builder = PrivacyLists.newBuilder();
        for (String type : requestedTypes) {
            builder.addLists(com.halloapp.proto.server.PrivacyList.newBuilder().setType(com.halloapp.proto.server.PrivacyList.Type.valueOf(type.toUpperCase(Locale.US))));
        }
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setPrivacyLists(builder.build());
    }
}
