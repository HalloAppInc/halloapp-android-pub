package com.halloapp.xmpp.privacy;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.UidElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrivacyList {

    static final String TYPE_ADD = "add";
    static final String TYPE_DELETE = "delete";

    public final Map<UserId, String> typeMap = new HashMap<>();
    public final List<UserId> userIds = new ArrayList<>();

    public @Type String type;

    @StringDef({Type.ALL, Type.EXCEPT, Type.ONLY, Type.MUTE, Type.BLOCK, Type.INVALID})
    public @interface Type {
        String INVALID = "invalid";
        String ALL = "all"; // All mutually connected people
        String EXCEPT = "except"; // Blacklist
        String ONLY = "only"; // Whitelist
        String MUTE = "mute"; // Hide content from specific people (feed only)
        String BLOCK = "block"; // Blocks all communication
    }

    PrivacyList(com.halloapp.proto.server.PrivacyList privacyList) {
        this.type = privacyList.getType().name().toLowerCase(Locale.US);
        for (UidElement uidElement : privacyList.getUidElementsList()) {
            UserId userId = new UserId(Long.toString(uidElement.getUid()));
            userIds.add(userId);
            if (uidElement.getAction().equals(UidElement.Action.ADD)) {
                typeMap.put(userId, TYPE_ADD);
            } else if (uidElement.getAction().equals(UidElement.Action.DELETE)) {
                typeMap.put(userId, TYPE_DELETE);
            }
        }
    }

    @NonNull
    public List<UserId> getUserIds() {
        return userIds;
    }
}
