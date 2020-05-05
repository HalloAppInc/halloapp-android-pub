package com.halloapp.content;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;

public abstract class ContentItem {

    public long rowId;
    public final UserId senderUserId;
    public final String id;
    public final long timestamp;

    public final String text;
    public final List<Media> media = new ArrayList<>();

    public ContentItem(
            long rowId,
            UserId senderUserId,
            String id,
            long timestamp,
            String text) {
        this.rowId = rowId;
        this.senderUserId = senderUserId;
        this.id = id;
        this.timestamp = timestamp;
        this.text = TextUtils.isEmpty(text) ? null : text;
    }

    public abstract void addToStorage(@NonNull ContentDb contentDb);
    public abstract void send(@NonNull Connection connection);
    public abstract void setMediaTransferred(@NonNull Media media, @NonNull ContentDb contentDb);

    public boolean isOutgoing() {
        return senderUserId.isMe();
    }

    public boolean isIncoming() {
        return !isOutgoing();
    }

    public boolean isRetracted() {
        return TextUtils.isEmpty(text) && media.isEmpty();
    }

    public boolean isAllMediaTransferred() {
        for (Media mediaItem : media) {
            if (!mediaItem.transferred) {
                return false;
            }
        }
        return true;
    }
}
