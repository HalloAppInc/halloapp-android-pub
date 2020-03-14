package com.halloapp.ui;

import android.content.Intent;

import androidx.collection.LongSparseArray;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.ui.avatar.AvatarLoader;

public interface ContentViewHolderParent {

    AvatarLoader getAvatarLoader();
    ContactLoader getContactLoader();
    LongSparseArray<Integer> getMediaPagerPositionMap();
    TimestampRefresher getTimestampRefresher();
    void startActivity(Intent intent);
}
