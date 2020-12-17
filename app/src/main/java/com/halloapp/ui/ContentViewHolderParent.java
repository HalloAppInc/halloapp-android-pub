package com.halloapp.ui;

import android.content.Intent;

import androidx.collection.LongSparseArray;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.ChatLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.SeenByLoader;

public interface ContentViewHolderParent {

    AvatarLoader getAvatarLoader();
    ContactLoader getContactLoader();
    ChatLoader getChatLoader();
    SeenByLoader getSeenByLoader();
    TextContentLoader getTextContentLoader();
    LongSparseArray<Integer> getMediaPagerPositionMap();
    LongSparseArray<Integer> getTextLimits();
    TimestampRefresher getTimestampRefresher();
    void startActivity(Intent intent);
}
