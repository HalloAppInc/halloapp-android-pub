package com.halloapp.ui.posts;

import android.content.Intent;
import android.view.View;

import androidx.collection.LongSparseArray;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.TimestampRefresher;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.widget.DrawDelegateView;

import java.util.Stack;

public interface PostViewHolderParent {

    AvatarLoader getAvatarLoader();
    ContactLoader getContactLoader();
    DrawDelegateView getDrawDelegateView();
    MediaThumbnailLoader getMediaThumbnailLoader();
    LongSparseArray<Integer> getMediaPagerPositionMap();
    Stack<View> getRecycledMediaViews();
    TimestampRefresher getTimestampRefresher();
    void startActivity(Intent intent);
}
