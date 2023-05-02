package com.halloapp.ui;

import android.content.Intent;

import androidx.collection.LongSparseArray;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.groups.GroupLoader;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.SeenByLoader;

public interface ContentViewHolderParent {

    AvatarLoader getAvatarLoader();
    ContactLoader getContactLoader();
    ReactionLoader getReactionLoader();
    SystemMessageTextResolver getSystemMessageTextResolver();
    GroupLoader getGroupLoader();
    SeenByLoader getSeenByLoader();
    TextContentLoader getTextContentLoader();
    AudioDurationLoader getAudioDurationLoader();
    LongSparseArray<Integer> getMediaPagerPositionMap();
    LongSparseArray<Integer> getTextLimits();
    TimestampRefresher getTimestampRefresher();
    void startActivity(Intent intent);
}
