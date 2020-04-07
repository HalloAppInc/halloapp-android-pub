package com.halloapp.ui;

import android.content.Intent;

import androidx.collection.LongSparseArray;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.posts.SeenByLoader;

public interface ContentViewHolderParent {

    AvatarLoader getAvatarLoader();
    ContactLoader getContactLoader();
    SeenByLoader getSeenByLoader();
    LongSparseArray<Integer> getMediaPagerPositionMap();
    TimestampRefresher getTimestampRefresher();
    void startActivity(Intent intent);
}
