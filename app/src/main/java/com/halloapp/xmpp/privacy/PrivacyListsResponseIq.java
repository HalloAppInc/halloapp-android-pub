package com.halloapp.xmpp.privacy;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.PrivacyLists;
import com.halloapp.xmpp.HalloIq;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrivacyListsResponseIq extends HalloIq {

    public final @PrivacyList.Type String activeType;

    public final Map<String, PrivacyList> resultMap;

    private PrivacyListsResponseIq(String activeType, Map<String, PrivacyList> resultMap) {
        this.activeType = activeType;
        this.resultMap = resultMap;
    }

    @Nullable
    public PrivacyList getPrivacyList(@PrivacyList.Type String type) {
        if (resultMap.containsKey(type)) {
            return resultMap.get(type);
        }
        return null;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static PrivacyListsResponseIq fromProto(PrivacyLists privacyLists) {
        String activeType = privacyLists.getActiveType().name().toLowerCase(Locale.US);
        Map<String, PrivacyList> resultMap = new HashMap<>();

        for (com.halloapp.proto.server.PrivacyList privacyList : privacyLists.getListsList()) {
            PrivacyList list = new PrivacyList(privacyList);
            resultMap.put(list.type, list);
        }

        return new PrivacyListsResponseIq(activeType, resultMap);
    }
}
