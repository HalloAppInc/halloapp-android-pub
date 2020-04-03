package com.halloapp.content;

import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public abstract class ContentItem {

    public long rowId;
    public final UserId senderUserId;
    public final String id;
    public final long timestamp;

    public final @TransferredState int transferred;
    public @SeenState int seen;

    public final String text;
    public final List<Media> media = new ArrayList<>();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SEEN_NO, SEEN_YES_PENDING, SEEN_YES})
    public @interface SeenState {}
    public static final int SEEN_NO = 0;
    public static final int SEEN_YES_PENDING = 1;
    public static final int SEEN_YES = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_NO, TRANSFERRED_SERVER, TRANSFERRED_DESTINATION})
    public @interface TransferredState {}
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_SERVER = 1;
    public static final int TRANSFERRED_DESTINATION = 2;

    public ContentItem(
            long rowId,
            UserId senderUserId,
            String id,
            long timestamp,
            @TransferredState int transferred,
            @SeenState int seen,
            String text) {
        this.rowId = rowId;
        this.senderUserId = senderUserId;
        this.id = id;
        this.timestamp = timestamp;
        this.transferred = transferred;
        this.seen = seen;
        this.text = text;
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
