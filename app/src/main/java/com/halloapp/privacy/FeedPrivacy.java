package com.halloapp.privacy;

import androidx.annotation.Nullable;

import com.halloapp.id.UserId;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedPrivacy {

    public final @PrivacyList.Type String activeList;

    public final List<UserId> onlyList;
    public final List<UserId> exceptList;

    public FeedPrivacy(@PrivacyList.Type String activeSetting, @Nullable List<UserId> exceptList, @Nullable List<UserId> onlyList) {
        this.activeList = activeSetting;
        this.onlyList = onlyList != null ? new ArrayList<>(onlyList) : Collections.emptyList();
        this.exceptList = exceptList != null ? new ArrayList<>(exceptList) : Collections.emptyList();
    }
}
